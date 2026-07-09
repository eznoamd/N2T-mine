package com.enzo.n2tmine.ic.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

/** ScreenHandler do menu do bloco de saida. Sem slots -- campo de nome + botoes. */
public class IcExitScreenHandler extends ScreenHandler {

    private final int roomId;
    private final String roomName;

    // Construtor usado no CLIENTE (via ExtendedScreenHandlerType.Factory).
    public IcExitScreenHandler(int syncId, PlayerInventory inv, IcExitData data) {
        this(syncId, data.roomId(), data.name());
    }

    // Construtor usado no SERVIDOR.
    public IcExitScreenHandler(int syncId, int roomId, String roomName) {
        super(ModScreenHandlers.IC_EXIT, syncId);
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
