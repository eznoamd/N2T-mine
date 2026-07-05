package com.enzo.n2tmine.ic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class IcPortBlockEntity extends BlockEntity {

    private int injectedPower = 0;
    private Direction inwardDirection = Direction.NORTH; // guardado so pra referencia/depuracao

    public IcPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModIcBlockEntities.IC_PORT_BLOCK_ENTITY, pos, state);
    }

    public int getInjectedPower() {
        return injectedPower;
    }

    public void setInwardDirection(Direction direction) {
        this.inwardDirection = direction;
        markDirty();
    }

    public void setInjectedPower(ServerWorld icWorld, BlockPos pos, int power) {
        if (power != this.injectedPower) {
            this.injectedPower = power;
            markDirty();
            icWorld.updateNeighborsAlways(pos, getCachedState().getBlock());
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("injectedPower", injectedPower);
        nbt.putString("inwardDirection", inwardDirection.getName());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        injectedPower = nbt.getInt("injectedPower");
        String dirName = nbt.getString("inwardDirection");
        for (Direction dir : Direction.values()) {
            if (dir.getName().equals(dirName)) {
                inwardDirection = dir;
                break;
            }
        }
    }
}
