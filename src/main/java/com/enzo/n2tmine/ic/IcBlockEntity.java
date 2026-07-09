package com.enzo.n2tmine.ic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class IcBlockEntity extends BlockEntity {

    private static final Direction[] SIDES = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

    // masterRoomId = o "design" (sala editavel, compartilhada por todas as copias).
    // instanceRoomId = a sala-clone privada deste bloco, onde o I/O dele realmente roda.
    private int masterRoomId = -1;
    private int instanceRoomId = -1;

    private final Map<Direction, Integer> outputPower = new EnumMap<>(Direction.class);

    // Transiente: re-forca mestre+instancia uma vez por carregamento.
    private boolean chunkForced = false;

    public IcBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IC_BLOCK_ENTITY, pos, state);
        for (Direction dir : SIDES) {
            outputPower.put(dir, 0);
        }
    }

    public int getOutputPower(Direction direction) {
        return outputPower.getOrDefault(direction, 0);
    }

    /** O design (mestre) deste bloco -- e isso que vai no item ao quebrar. */
    public int getDesignId() {
        return masterRoomId;
    }

    public int getInstanceRoomId() {
        return instanceRoomId;
    }

    /** Bloco colocado a partir de um item que ja tinha design: entra nesse design
     *  e ganha uma instancia (clone) privada. */
    public void adoptRoom(ServerWorld outerWorld, int existingMasterId) {
        this.masterRoomId = existingMasterId;
        this.instanceRoomId = IcRoomState.get(outerWorld.getServer())
                .createInstance(outerWorld.getServer(), existingMasterId);
        markDirty();
    }

    /** Usado pela clonagem de sala pra religar um CI aninhado ao design correto
     *  e a instancia recem-criada pra ele. */
    public void setDesignAndInstance(int masterRoomId, int instanceRoomId) {
        this.masterRoomId = masterRoomId;
        this.instanceRoomId = instanceRoomId;
        this.chunkForced = true; // ja forcado na criacao da instancia
        markDirty();
    }

    public void teleportPlayerInside(ServerWorld outerWorld, ServerPlayerEntity player) {
        ensureRoom(outerWorld);
        ServerWorld icWorld = outerWorld.getServer().getWorld(ModDimensions.IC_WORLD);
        if (icWorld == null) return;
        IcRoomState rooms = IcRoomState.get(outerWorld.getServer());

        // O jogador entra sempre no MESTRE (pra editar o design). O destino de
        // retorno do mestre aponta pra ESTE bloco (ultima entrada usada).
        BlockPos exitPos = rooms.getExitPos(masterRoomId);
        if (icWorld.getBlockEntity(exitPos) instanceof IcExitBlockEntity exitBe) {
            exitBe.setReturnLocation(outerWorld.getRegistryKey(), pos);
        }

        BlockPos origin = rooms.getRoomOrigin(masterRoomId);
        int mid = (IcRoomState.SIZE - 1) / 2;
        BlockPos target = origin.add(mid, 1, mid);

        player.teleport(icWorld, target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                Set.<PositionFlag>of(), 0f, 0f);
    }

    private void ensureRoom(ServerWorld outerWorld) {
        IcRoomState rooms = IcRoomState.get(outerWorld.getServer());
        if (masterRoomId < 0) {
            // Bloco totalmente novo: cria um design (mestre vazio) e uma instancia.
            masterRoomId = rooms.createDesign(outerWorld);
            instanceRoomId = rooms.createInstance(outerWorld.getServer(), masterRoomId);
            markDirty();
        } else if (instanceRoomId < 0) {
            // Tem design (veio de item/copia) mas ainda sem instancia: cria a instancia.
            instanceRoomId = rooms.createInstance(outerWorld.getServer(), masterRoomId);
            markDirty();
        }
        if (!chunkForced && masterRoomId >= 0 && instanceRoomId >= 0) {
            rooms.forceRoom(outerWorld.getServer(), masterRoomId);
            rooms.forceRoom(outerWorld.getServer(), instanceRoomId);
            chunkForced = true;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, IcBlockEntity be) {
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;

        be.ensureRoom(serverWorld);
        ServerWorld icWorld = serverWorld.getServer().getWorld(ModDimensions.IC_WORLD);
        if (icWorld == null) return;

        // O I/O deste bloco roda na sua INSTANCIA privada (nao no mestre).
        BlockPos origin = IcRoomState.get(serverWorld.getServer()).getRoomOrigin(be.instanceRoomId);
        int max = IcRoomState.SIZE - 1;
        int mid = max / 2;
        int midY = IcRoomState.PORT_Y;

        boolean anyChanged = false;

        for (Direction dir : SIDES) {
            BlockPos portPos = portPosition(origin, dir, mid, midY, max);
            if (!(icWorld.getBlockEntity(portPos) instanceof IcPortBlockEntity portBe)) {
                continue;
            }

            if (portBe.getRole() == PortRole.INPUT) {
                int externalInput = serverWorld.getEmittedRedstonePower(pos.offset(dir), dir);
                portBe.setInjectedPower(icWorld, portPos, externalInput);

                if (be.outputPower.getOrDefault(dir, 0) != 0) {
                    be.outputPower.put(dir, 0);
                    anyChanged = true;
                }
            } else {
                portBe.setInjectedPower(icWorld, portPos, 0);

                int internalReading = icWorld.getReceivedRedstonePower(portPos);

                int previous = be.outputPower.getOrDefault(dir, 0);
                if (internalReading != previous) {
                    be.outputPower.put(dir, internalReading);
                    anyChanged = true;
                }
            }
        }

        if (anyChanged) {
            serverWorld.updateNeighborsAlways(pos, state.getBlock());
            be.markDirty();
        }
    }

    private static BlockPos portPosition(BlockPos origin, Direction dir, int mid, int midY, int max) {
        return switch (dir) {
            case NORTH -> origin.add(mid, midY, 0);
            case SOUTH -> origin.add(mid, midY, max);
            case WEST -> origin.add(0, midY, mid);
            case EAST -> origin.add(max, midY, mid);
            default -> origin;
        };
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("masterRoomId", masterRoomId);
        nbt.putInt("instanceRoomId", instanceRoomId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        // compat: se existir o "roomId" antigo, usa como master (design).
        if (nbt.contains("masterRoomId")) {
            masterRoomId = nbt.getInt("masterRoomId");
        } else if (nbt.contains("roomId")) {
            masterRoomId = nbt.getInt("roomId");
        }
        instanceRoomId = nbt.contains("instanceRoomId") ? nbt.getInt("instanceRoomId") : -1;
    }
}
