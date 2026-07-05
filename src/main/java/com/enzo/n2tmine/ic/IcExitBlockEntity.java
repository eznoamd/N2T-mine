package com.enzo.n2tmine.ic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class IcExitBlockEntity extends BlockEntity {

    private Identifier returnDim;
    private BlockPos returnPos;

    public IcExitBlockEntity(BlockPos pos, BlockState state) {
        super(ModIcBlockEntities.IC_EXIT_BLOCK_ENTITY, pos, state);
    }

    public void setReturnLocation(RegistryKey<World> dim, BlockPos pos) {
        this.returnDim = dim.getValue();
        this.returnPos = pos;
        markDirty();
    }

    public void teleportPlayerOut(ServerWorld icWorld, ServerPlayerEntity player) {
        if (returnDim == null || returnPos == null) return;
        ServerWorld destination = icWorld.getServer().getWorld(
                RegistryKey.of(RegistryKeys.WORLD, returnDim)
        );
        if (destination == null) return;

        player.teleport(destination, returnPos.getX() + 0.5, returnPos.getY() + 1, returnPos.getZ() + 0.5,
                Set.<PositionFlag>of(), 0f, 0f);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (returnDim != null) nbt.putString("returnDim", returnDim.toString());
        if (returnPos != null) nbt.putLong("returnPos", returnPos.asLong());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("returnDim")) {
            returnDim = Identifier.of(nbt.getString("returnDim"));
        }
        if (nbt.contains("returnPos")) {
            returnPos = BlockPos.fromLong(nbt.getLong("returnPos"));
        }
    }
}
