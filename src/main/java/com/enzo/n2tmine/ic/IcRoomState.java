package com.enzo.n2tmine.ic;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Guarda o mapeamento "bloco IC no mundo real" -> "sala dentro da dimensao do CI",
 * e sabe como construir fisicamente essas salas.
 *
 * Geometria da sala (ajuste aqui se quiser salas maiores/menores):
 *   SIZE x HEIGHT x SIZE, incluindo paredes/chao/teto.
 */
public class IcRoomState extends PersistentState {

    public static final int SIZE = 9;      // 0..8, paredes em 0 e 8
    public static final int HEIGHT = 5;    // 0..4, chao em 0, teto em 4
    public static final int SPACING = 32;  // distancia entre salas na dimensao do CI
    public static final int BASE_Y = 100;

    private final Map<String, Integer> allocatedRooms = new HashMap<>();
    private int nextRoomId = 0;

    public static final PersistentState.Type<IcRoomState> TYPE = new PersistentState.Type<>(
            IcRoomState::new,
            IcRoomState::createFromNbt,
            null
    );

    public static IcRoomState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(TYPE, "n2t_mine_ic_rooms");
    }

    private static String key(RegistryKey<World> dim, BlockPos pos) {
        return dim.getValue() + "@" + pos.asLong();
    }

    /** Retorna o roomId existente, ou cria e constroi uma sala nova se ainda nao existir. */
    public int getOrCreateRoom(ServerWorld outerWorld, BlockPos outerPos) {
        String k = key(outerWorld.getRegistryKey(), outerPos);
        Integer existing = allocatedRooms.get(k);
        if (existing != null) {
            return existing;
        }

        int roomId = nextRoomId++;
        allocatedRooms.put(k, roomId);
        markDirty();

        MinecraftServer server = outerWorld.getServer();
        ServerWorld icWorld = server.getWorld(ModDimensions.IC_WORLD);
        if (icWorld != null) {
            buildRoom(icWorld, roomId, outerWorld.getRegistryKey(), outerPos);
        }
        return roomId;
    }

    public BlockPos getRoomOrigin(int roomId) {
        return new BlockPos(roomId * SPACING, BASE_Y, 0);
    }

    private void buildRoom(ServerWorld icWorld, int roomId, RegistryKey<World> outerDim, BlockPos outerPos) {
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
        int midY = HEIGHT / 2;

        placePort(icWorld, origin.add(mid, midY, 0), Direction.NORTH);
        placePort(icWorld, origin.add(mid, midY, max), Direction.SOUTH);
        placePort(icWorld, origin.add(0, midY, mid), Direction.WEST);
        placePort(icWorld, origin.add(max, midY, mid), Direction.EAST);

        BlockPos exitPos = origin.add(mid, 1, mid);
        icWorld.setBlockState(exitPos, ModIcBlocks.IC_EXIT_BLOCK.getDefaultState());
        if (icWorld.getBlockEntity(exitPos) instanceof IcExitBlockEntity exitBe) {
            exitBe.setReturnLocation(outerDim, outerPos);
        }
    }

    /** outwardDirection = a face do bloco LA FORA que essa porta representa (ex: NORTH). */
    private void placePort(ServerWorld icWorld, BlockPos pos, Direction outwardDirection) {
        icWorld.setBlockState(pos, ModIcBlocks.IC_PORT_BLOCK.getDefaultState());
        if (icWorld.getBlockEntity(pos) instanceof IcPortBlockEntity portBe) {
            portBe.setInwardDirection(outwardDirection.getOpposite());
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("nextRoomId", nextRoomId);
        NbtCompound rooms = new NbtCompound();
        allocatedRooms.forEach(rooms::putInt);
        nbt.put("rooms", rooms);
        return nbt;
    }

    public static IcRoomState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        IcRoomState state = new IcRoomState();
        state.nextRoomId = nbt.getInt("nextRoomId");
        NbtCompound rooms = nbt.getCompound("rooms");
        for (String k : rooms.getKeys()) {
            state.allocatedRooms.put(k, rooms.getInt(k));
        }
        return state;
    }
}
