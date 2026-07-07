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
    private long savedMessageUntil = 0L; // timestamp (ms) ate quando mostrar "Salvo!"

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
            this.savedMessageUntil = System.currentTimeMillis() + 2000L; // mostra "Salvo!" por 2s
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
        // Painel de fundo simples (sem textura customizada)
        context.fill(left, top, left + this.backgroundWidth, top + this.backgroundHeight, 0xF0202020);
        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF000000);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // So o titulo; nao desenhamos o label de inventario (nao ha inventario).
        context.drawText(this.textRenderer, this.title, 20, 12, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Nome da sala:"), 20, 30, 0xA0A0A0, false);

        // Mensagem "Salvo!" em verde, por alguns segundos apos salvar.
        if (System.currentTimeMillis() < this.savedMessageUntil) {
            context.drawText(this.textRenderer, Text.literal("Salvo!"), 110, 30, 0x55FF55, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // HandledScreen.render ja desenha o fundo escurecido + drawBackground +
        // drawForeground; so acrescentamos o tooltip por cima.
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
