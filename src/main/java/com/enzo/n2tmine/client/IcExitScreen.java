package com.enzo.n2tmine.client;

import com.enzo.n2tmine.ic.net.ExitRoomPayload;
import com.enzo.n2tmine.ic.net.RenameRoomPayload;
import com.enzo.n2tmine.ic.screen.IcExitScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class IcExitScreen extends HandledScreen<IcExitScreenHandler> {

    private TextFieldWidget nameField;
    private long savedMessageUntil = 0L;

    public IcExitScreen(IcExitScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 220;
        this.backgroundHeight = 140;
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - this.backgroundWidth) / 2;
        int top = (this.height - this.backgroundHeight) / 2;

        // Campo de texto pro nome da sala
        this.nameField = new TextFieldWidget(this.textRenderer, left + 20, top + 40, 180, 20,
                Text.literal("Nome da sala"));
        this.nameField.setMaxLength(48);
        this.nameField.setText(this.handler.getRoomName());
        this.nameField.setPlaceholder(Text.literal("Nome da sala..."));
        addDrawableChild(this.nameField);
        setInitialFocus(this.nameField);

        // Botao: salvar nome
        addDrawableChild(ButtonWidget.builder(Text.literal("Salvar nome"), b -> {
            ClientPlayNetworking.send(new RenameRoomPayload(this.handler.getRoomId(), this.nameField.getText()));
            this.savedMessageUntil = System.currentTimeMillis() + 2000L;
        }).dimensions(left + 20, top + 68, 180, 20).build());

        // Botao: sair da sala
        addDrawableChild(ButtonWidget.builder(Text.literal("Sair da sala"), b -> {
            ClientPlayNetworking.send(new ExitRoomPayload(this.handler.getRoomId()));
            this.close();
        }).dimensions(left + 20, top + 96, 88, 20).build());

        // Botao: cancelar (fecha sem sair)
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancelar"), b -> this.close())
                .dimensions(left + 112, top + 96, 88, 20).build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = (this.width - this.backgroundWidth) / 2;
        int top = (this.height - this.backgroundHeight) / 2;
        context.fill(left, top, left + this.backgroundWidth, top + this.backgroundHeight, 0xF0202020);
        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF000000);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, 20, 12, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Nome da sala:"), 20, 30, 0xA0A0A0, false);
        if (System.currentTimeMillis() < this.savedMessageUntil) {
            context.drawText(this.textRenderer, Text.literal("Salvo!"), 110, 30, 0x55FF55, false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Se o campo de nome esta focado, ELE processa a tecla PRIMEIRO. Sem isso,
        // a HandledScreen intercepta a tecla de inventario (E) e fecha o menu antes
        // do campo receber a letra -- e por isso o 'e' fechava a janela.
        // ENTER (257/335) e ESC (256) seguem o comportamento normal.
        if (this.nameField != null && this.nameField.isFocused()
                && keyCode != 257 && keyCode != 335 && keyCode != 256) {
            this.nameField.keyPressed(keyCode, scanCode, modifiers);
            return true; // a letra em si entra pelo charTyped abaixo
        }
        // Fora do campo, a tecla de inventario (E) tambem nao fecha o menu.
        if (this.client != null
                && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Envia a digitacao (inclusive 'e') pro campo focado.
        if (this.nameField != null && this.nameField.isFocused()) {
            return this.nameField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
