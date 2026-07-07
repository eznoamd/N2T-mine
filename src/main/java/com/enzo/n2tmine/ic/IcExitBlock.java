package com.enzo.n2tmine.ic;

import com.enzo.n2tmine.ic.screen.IcExitData;
import com.enzo.n2tmine.ic.screen.IcExitScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IcExitBlock extends Block implements BlockEntityProvider {

    public IcExitBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IcExitBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        // Deriva o roomId pela posicao e busca o nome atual da sala.
        int roomId = IcRoomState.roomIdFromInsidePos(pos);
        String name = IcRoomState.get(serverWorld.getServer()).getRoomName(roomId);

        // Abre o menu (renomear / sair / cancelar). O teleporte de saida agora
        // acontece pelo botao "Sair" do menu (via pacote de rede).
        serverPlayer.openHandledScreen(new ExtendedScreenHandlerFactory<IcExitData>() {
            @Override
            public Text getDisplayName() {
                return Text.literal("Circuito Integrado");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
                return new IcExitScreenHandler(syncId, roomId, name);
            }

            @Override
            public IcExitData getScreenOpeningData(ServerPlayerEntity p) {
                return new IcExitData(roomId, name);
            }
        });
        return ActionResult.CONSUME;
    }
}
