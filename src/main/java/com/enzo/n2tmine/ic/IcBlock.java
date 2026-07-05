package com.enzo.n2tmine.ic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IcBlock extends Block implements BlockEntityProvider {

    public IcBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IcBlockEntity(pos, state);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof IcBlockEntity be) {
            return be.getOutputPower(direction);
        }
        return 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakRedstonePower(state, world, pos, direction);
    }

    // ATENCAO: a assinatura exata deste metodo (onUse) muda com frequencia entre
    // versoes do Minecraft. Se der erro de compilacao aqui, deixe o IDE sugerir
    // a assinatura correta pra sua versao (geralmente so muda a ordem/presenca
    // do parametro Hand) e me manda o erro que eu ajusto.
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }
        if (world.getBlockEntity(pos) instanceof IcBlockEntity be) {
            be.teleportPlayerInside(serverWorld, serverPlayer);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModIcBlockEntities.IC_BLOCK_ENTITY) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<IcBlockEntity>) IcBlockEntity::tick;
    }
}
