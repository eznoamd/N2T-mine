package com.enzo.n2tmine.client;

import com.enzo.n2tmine.ic.ModIcBlockEntities;
import com.enzo.n2tmine.ic.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class N2TMineClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Liga o ScreenHandler (logico) a Screen (visual) do menu do bloco de saida.
        HandledScreens.register(ModScreenHandlers.IC_EXIT, IcExitScreen::new);

        // Renderizador que desenha o nome do CI flutuando acima do bloco.
        BlockEntityRendererRegistry.register(ModIcBlockEntities.IC_BLOCK_ENTITY, IcBlockEntityRenderer::new);
    }
}
