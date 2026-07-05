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

    private int roomId = -1;
    private final Map<Direction, Integer> outputPower = new EnumMap<>(Direction.class);

    public IcBlockEntity(BlockPos pos, BlockState state) {
        super(ModIcBlockEntities.IC_BLOCK_ENTITY, pos, state);
        for (Direction dir : SIDES) {
            outputPower.put(dir, 0);
        }
    }

    public int getOutputPower(Direction direction) {
        return outputPower.getOrDefault(direction, 0);
    }

    public void teleportPlayerInside(ServerWorld outerWorld, ServerPlayerEntity player) {
        ensureRoom(outerWorld);
        ServerWorld icWorld = outerWorld.getServer().getWorld(ModDimensions.IC_WORLD);
        if (icWorld == null) return;

        BlockPos origin = IcRoomState.get(outerWorld.getServer()).getRoomOrigin(roomId);
        int mid = (IcRoomState.SIZE - 1) / 2;
        BlockPos target = origin.add(mid, 1, mid);

        // ATENCAO: a assinatura de ServerPlayerEntity#teleport tambem varia por
        // versao. Se der erro, seu IDE vai mostrar a assinatura certa disponivel.
        player.teleport(icWorld, target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                Set.<PositionFlag>of(), 0f, 0f);
    }

    private void ensureRoom(ServerWorld outerWorld) {
        if (roomId < 0) {
            roomId = IcRoomState.get(outerWorld.getServer()).getOrCreateRoom(outerWorld, pos);
            markDirty();
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, IcBlockEntity be) {
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;

        be.ensureRoom(serverWorld);
        ServerWorld icWorld = serverWorld.getServer().getWorld(ModDimensions.IC_WORLD);
        if (icWorld == null) return;

        BlockPos origin = IcRoomState.get(serverWorld.getServer()).getRoomOrigin(be.roomId);
        int max = IcRoomState.SIZE - 1;
        int mid = max / 2;
        int midY = IcRoomState.HEIGHT / 2;

        boolean anyChanged = false;

        for (Direction dir : SIDES) {
            // 1) Le o sinal externo chegando nessa face do bloco visivel
            BlockPos neighborPos = pos.offset(dir);
            int externalInput = serverWorld.getEmittedRedstonePower(neighborPos, dir.getOpposite());

            // 2) Empurra esse valor pra porta correspondente dentro da sala
            BlockPos portPos = portPosition(origin, dir, mid, midY, max);
            if (icWorld.getBlockEntity(portPos) instanceof IcPortBlockEntity portBe) {
                portBe.setInjectedPower(icWorld, portPos, externalInput);

                // 3) Le o que esta acontecendo dentro da sala, do lado de dentro dessa porta
                Direction inward = dir.getOpposite();
                BlockPos insidePos = portPos.offset(inward);
                int internalReading = icWorld.getEmittedRedstonePower(insidePos, inward.getOpposite());

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
        nbt.putInt("roomId", roomId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        roomId = nbt.getInt("roomId");
    }
}
