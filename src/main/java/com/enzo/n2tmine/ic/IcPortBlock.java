package com.enzo.n2tmine.ic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IcPortBlock extends Block implements BlockEntityProvider {

    // false = ENTRADA (lapis), true = SAIDA (esmeralda). O modelo troca conforme
    // esse valor (ver blockstates/ic_port_block.json).
    public static final BooleanProperty OUTPUT = BooleanProperty.of("output");

    public IcPortBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(OUTPUT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OUTPUT);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IcPortBlockEntity(pos, state);
    }

    // Clique direito com a MAO VAZIA alterna a porta entre ENTRADA e SAIDA.
    // Com item na mao, deixa passar (PASS) pra voce continuar construindo normal.
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getMainHandStack().isEmpty()) {
            return ActionResult.PASS;
        }
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (world instanceof ServerWorld serverWorld
                && world.getBlockEntity(pos) instanceof IcPortBlockEntity be) {
            PortRole novo = be.cycleRole(serverWorld, pos);
            player.sendMessage(Text.literal("Porta agora e: " + novo.displayPt()), true);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof IcPortBlockEntity be) {
            return be.getInjectedPower();
        }
        return 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakRedstonePower(state, world, pos, direction);
    }
}
