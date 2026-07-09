package com.enzo.n2tmine.ic.screen;

import com.enzo.n2tmine.N2TMine;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers {

    public static final ScreenHandlerType<IcExitScreenHandler> IC_EXIT = Registry.register(
            Registries.SCREEN_HANDLER,
            N2TMine.id("ic_exit"),
            new ExtendedScreenHandlerType<>(IcExitScreenHandler::new, IcExitData.CODEC)
    );

    public static void initialize() {
    }
}
