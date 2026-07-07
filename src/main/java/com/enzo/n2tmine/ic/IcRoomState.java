package com.enzo.n2tmine.ic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Modelo "MESTRE + INSTANCIAS":
 *
 *  - Uma sala MESTRE por design: onde o jogador EDITA o circuito. E la que o
 *    jogador entra ao clicar num bloco de CI.
 *  - Uma sala INSTANCIA por bloco colocado: um clone fisico do mestre que roda
 *    com o I/O daquele bloco especifico. O jogador nunca visita as instancias.
 *  - Re-sincronizacao: ao sair do mestre, todas as instancias daquele design
 *    sao reclonadas do mestre (editar 1x propaga pra todas).
 *
 * O "designId" e simplesmente o roomId da sala mestre.
 *
 * Geometria: SIZE x HEIGHT x SIZE, portas em PORT_Y, ouro (exit) no centro do piso.
 */
public class IcRoomState extends PersistentState {

    public static final int SIZE = 15;     // 0..14 -> piso util 13x13
    public static final int HEIGHT = 7;    // 0..6 -> 5 de altura interna
    public static final int SPACING = 32;  // distancia entre salas (multiplo de 16)
    public static final int BASE_Y = 100;
    public static final int PORT_Y = 1;    // altura das portas (1 acima do ouro)

    private int nextRoomId = 0;
    // master (design) -> conjunto de instancias clonadas dele
    private final Map<Integer, Set<Integer>> masterInstances = new HashMap<>();
    // nome por design (master)
    private final Map<Integer, String> roomNames = new HashMap<>();

    public static final PersistentState.Type<IcRoomState> TYPE = new PersistentState.Type<>(
            IcRoomState::new,
            IcRoomState::createFromNbt,
            null
    );

    public static IcRoomState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(TYPE, "n2t_mine_ic_rooms");
    }

    // ---------------------------------------------------------------- geometria

    public BlockPos getRoomOrigin(int roomId) {
        return new BlockPos(roomId * SPACING, BASE_Y, 0);
    }

    public BlockPos getExitPos(int roomId) {
        int mid = (SIZE - 1) / 2;
        return getRoomOrigin(roomId).add(mid, 0, mid);
    }

    /** roomId da sala que contem `pos` (funciona pra mestre ou instancia). */
    public static int roomIdFromInsidePos(BlockPos pos) {
        return Math.floorDiv(pos.getX(), SPACING);
    }

    public void setRoomForced(ServerWorld icWorld, int roomId, boolean forced) {
        BlockPos origin = getRoomOrigin(roomId);
        icWorld.setChunkForced(origin.getX() >> 4, origin.getZ() >> 4, forced);
    }

    // ---------------------------------------------------------------- nomes

    public String getRoomName(int masterRoomId) {
        return roomNames.getOrDefault(masterRoomId, "");
    }

    public void renameRoom(int masterRoomId, String name) {
        String clean = name == null ? "" : name.strip();
        if (clean.isEmpty()) {
            roomNames.remove(masterRoomId);
        } else {
            if (clean.length() > 48) clean = clean.substring(0, 48);
            roomNames.put(masterRoomId, clean);
        }
        markDirty();
    }

    // ---------------------------------------------------------------- ciclo de vida

    /** Cria um design novo: constroi uma sala MESTRE vazia e devolve o masterRoomId. */
    public int createDesign(ServerWorld outerWorld) {
        int masterId = nextRoomId++;
        markDirty();
        ServerWorld icWorld = outerWorld.getServer().getWorld(ModDimensions.IC_WORLD);
        if (icWorld != null) {
            buildRoom(icWorld, masterId);
            setRoomForced(icWorld, masterId, true);
        }
        return masterId;
    }

    /** Cria uma INSTANCIA (clone do mestre) e devolve o instanceRoomId. */
    public int createInstance(MinecraftServer server, int masterRoomId) {
        return createInstance(server, masterRoomId, 0, new HashSet<>());
    }

    private int createInstance(MinecraftServer server, int masterRoomId, int depth, Set<Integer> ancestors) {
        int instanceId = nextRoomId++;
        masterInstances.computeIfAbsent(masterRoomId, k -> new HashSet<>()).add(instanceId);
        markDirty();
        ServerWorld icWorld = server.getWorld(ModDimensions.IC_WORLD);
        if (icWorld != null) {
            // O clone copia a regiao inteira do mestre (paredes, portas, exit e o
            // circuito -- incluindo CIs aninhados), entao nao precisa de buildRoom.
            cloneRoomBlocks(icWorld, masterRoomId, instanceId, depth, ancestors);
            setRoomForced(icWorld, instanceId, true);
        }
        return instanceId;
    }

    /** Libera uma instancia (quando o bloco dela e quebrado). O mestre permanece.
     *  Libera tambem, recursivamente, instancias de CIs aninhados dentro dela. */
    public void releaseInstance(MinecraftServer server, int masterRoomId, int instanceRoomId) {
        ServerWorld icWorld = server.getWorld(ModDimensions.IC_WORLD);
        if (icWorld != null) {
            releaseNestedInstances(server, icWorld, instanceRoomId);
        }
        Set<Integer> set = masterInstances.get(masterRoomId);
        if (set != null) {
            set.remove(instanceRoomId);
            if (set.isEmpty()) masterInstances.remove(masterRoomId);
        }
        markDirty();
        if (icWorld != null) {
            setRoomForced(icWorld, instanceRoomId, false);
        }
    }

    /** Percorre uma sala e libera as instancias de todos os CIs aninhados nela. */
    private void releaseNestedInstances(MinecraftServer server, ServerWorld icWorld, int roomId) {
        BlockPos origin = getRoomOrigin(roomId);
        int max = SIZE - 1;
        for (int x = 0; x <= max; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z <= max; z++) {
                    BlockPos p = origin.add(x, y, z);
                    if (icWorld.getBlockEntity(p) instanceof IcBlockEntity nested) {
                        int d = nested.getDesignId();
                        int inst = nested.getInstanceRoomId();
                        if (d >= 0 && inst >= 0) {
                            releaseInstance(server, d, inst); // recursivo
                        }
                    }
                }
            }
        }
    }

    /** Reclona o mestre em TODAS as instancias daquele design (chamado ao sair do mestre). */
    public void resyncInstances(MinecraftServer server, int masterRoomId) {
        Set<Integer> set = masterInstances.get(masterRoomId);
        if (set == null || set.isEmpty()) return;
        ServerWorld icWorld = server.getWorld(ModDimensions.IC_WORLD);
        if (icWorld == null) return;
        // Copia da lista pra evitar ConcurrentModification (o clone pode criar/liberar
        // instancias de CIs aninhados durante a iteracao).
        for (int instanceId : new ArrayList<>(set)) {
            cloneRoomBlocks(icWorld, masterRoomId, instanceId, 0, new HashSet<>());
        }
    }

    public void forceRoom(MinecraftServer server, int roomId) {
        ServerWorld icWorld = server.getWorld(ModDimensions.IC_WORLD);
        if (icWorld != null) setRoomForced(icWorld, roomId, true);
    }

    // ---------------------------------------------------------------- construcao/clonagem

    private void buildRoom(ServerWorld icWorld, int roomId) {
        BlockPos origin = getRoomOrigin(roomId);
        int max = SIZE - 1;

        for (int x = 0; x <= max; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z <= max; z++) {
                    BlockPos p = origin.add(x, y, z);
                    boolean isWall = x == 0 || x == max || z == 0 || z == max || y == 0 || y == HEIGHT - 1;
                    icWorld.setBlockState(p, isWall
                            ? Blocks.STONE_BRICKS.getDefaultState()
                            : Blocks.AIR.getDefaultState());
                }
            }
        }

        int mid = max / 2;
        int portY = PORT_Y;

        placePort(icWorld, origin.add(mid, portY, 0), Direction.NORTH);
        placePort(icWorld, origin.add(mid, portY, max), Direction.SOUTH);
        placePort(icWorld, origin.add(0, portY, mid), Direction.WEST);
        placePort(icWorld, origin.add(max, portY, mid), Direction.EAST);

        BlockPos exitPos = origin.add(mid, 0, mid);
        icWorld.setBlockState(exitPos, ModIcBlocks.IC_EXIT_BLOCK.getDefaultState());
    }

    private void placePort(ServerWorld icWorld, BlockPos pos, Direction outwardDirection) {
        icWorld.setBlockState(pos, ModIcBlocks.IC_PORT_BLOCK.getDefaultState());
        if (icWorld.getBlockEntity(pos) instanceof IcPortBlockEntity portBe) {
            portBe.setInwardDirection(outwardDirection.getOpposite());
        }
    }

    private static final int MAX_NEST_DEPTH = 8;

    /**
     * Copia TODO o conteudo (blocos + papeis das portas + CIs aninhados) da sala
     * `src` pra `dst`. CIs aninhados sao religados ao MESMO design do original e
     * ganham a propria instancia (recursivamente), com guarda contra ciclos.
     */
    public void cloneRoomBlocks(ServerWorld icWorld, int srcRoomId, int dstRoomId) {
        cloneRoomBlocks(icWorld, srcRoomId, dstRoomId, 0, new HashSet<>());
    }

    private void cloneRoomBlocks(ServerWorld icWorld, int srcRoomId, int dstRoomId, int depth, Set<Integer> ancestors) {
        MinecraftServer server = icWorld.getServer();

        // 0) Libera instancias de CIs aninhados que existiam na versao ANTIGA do
        //    destino (senao cada re-sync vazaria salas).
        releaseNestedInstances(server, icWorld, dstRoomId);

        BlockPos srcOrigin = getRoomOrigin(srcRoomId);
        BlockPos dstOrigin = getRoomOrigin(dstRoomId);
        int max = SIZE - 1;

        // Guarda de ciclo: nao reentrar num design ja presente na cadeia de ancestrais.
        Set<Integer> ancestorsNext = new HashSet<>(ancestors);
        ancestorsNext.add(srcRoomId);

        List<BlockPos> touched = new ArrayList<>();

        for (int x = 0; x <= max; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z <= max; z++) {
                    BlockPos from = srcOrigin.add(x, y, z);
                    BlockPos to = dstOrigin.add(x, y, z);
                    BlockState st = icWorld.getBlockState(from);
                    icWorld.setBlockState(to, st, Block.NOTIFY_LISTENERS);

                    // Porta: copia o papel (entrada/saida).
                    if (st.getBlock() instanceof IcPortBlock) {
                        if (icWorld.getBlockEntity(from) instanceof IcPortBlockEntity srcBe
                                && icWorld.getBlockEntity(to) instanceof IcPortBlockEntity dstBe) {
                            dstBe.copyRoleFrom(srcBe);
                        }
                    }

                    // CI aninhado: religa o clone ao MESMO design do original e da
                    // uma instancia propria pra ele (recursivo). Com guarda de ciclo
                    // e profundidade, um CI dentro dele mesmo apenas fica inerte.
                    if (st.getBlock() instanceof IcBlock) {
                        if (icWorld.getBlockEntity(from) instanceof IcBlockEntity srcNested
                                && icWorld.getBlockEntity(to) instanceof IcBlockEntity dstNested) {
                            int nestedDesign = srcNested.getDesignId();
                            if (nestedDesign >= 0 && depth < MAX_NEST_DEPTH
                                    && !ancestorsNext.contains(nestedDesign)) {
                                int nestedInstance = createInstance(server, nestedDesign, depth + 1, ancestorsNext);
                                dstNested.setDesignAndInstance(nestedDesign, nestedInstance);
                            }
                        }
                    }

                    if (!st.isAir()) touched.add(to);
                }
            }
        }

        // Passe de updates so nos blocos nao-vazios, pra o redstone reavaliar.
        for (BlockPos p : touched) {
            icWorld.updateNeighborsAlways(p, icWorld.getBlockState(p).getBlock());
        }
    }

    // ---------------------------------------------------------------- persistencia

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("nextRoomId", nextRoomId);

        NbtCompound instances = new NbtCompound();
        masterInstances.forEach((master, set) -> {
            int[] arr = set.stream().mapToInt(Integer::intValue).toArray();
            instances.put(Integer.toString(master), new NbtIntArray(arr));
        });
        nbt.put("masterInstances", instances);

        NbtCompound names = new NbtCompound();
        roomNames.forEach((id, name) -> names.putString(Integer.toString(id), name));
        nbt.put("roomNames", names);
        return nbt;
    }

    public static IcRoomState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        IcRoomState state = new IcRoomState();
        state.nextRoomId = nbt.getInt("nextRoomId");

        NbtCompound instances = nbt.getCompound("masterInstances");
        for (String k : instances.getKeys()) {
            try {
                int master = Integer.parseInt(k);
                int[] arr = instances.getIntArray(k);
                Set<Integer> set = new HashSet<>();
                for (int v : arr) set.add(v);
                state.masterInstances.put(master, set);
            } catch (NumberFormatException ignored) {
            }
        }

        NbtCompound names = nbt.getCompound("roomNames");
        for (String k : names.getKeys()) {
            try {
                state.roomNames.put(Integer.parseInt(k), names.getString(k));
            } catch (NumberFormatException ignored) {
            }
        }
        return state;
    }
}
