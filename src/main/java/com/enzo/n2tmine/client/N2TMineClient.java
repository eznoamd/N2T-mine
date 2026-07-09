package com.enzo.n2tmine.client;

import com.enzo.n2tmine.ic.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class N2TMineClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Liga o ScreenHandler (logico) a Screen (visual) do menu do bloco de saida.
        HandledScreens.register(ModScreenHandlers.IC_EXIT, IcExitScreen::new);
    }
}
