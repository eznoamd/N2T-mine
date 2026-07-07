package com.enzo.n2tmine.client;

import com.enzo.n2tmine.ic.IcBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class IcBlockEntityRenderer implements BlockEntityRenderer<IcBlockEntity> {

    private final TextRenderer textRenderer;

    public IcBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public boolean rendersOutsideBoundingBox(IcBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public void render(IcBlockEntity be, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        String name = be.getCachedName();
        if (name == null || name.isEmpty()) return;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        matrices.push();
        matrices.translate(0.5, 1.3, 0.5);           // acima da placa
        matrices.multiply(camera.getRotation());      // billboard
        matrices.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float x = -this.textRenderer.getWidth(name) / 2.0f;

        // CHAVE: desenha o texto no buffer de entidades e FORCA o flush aqui mesmo,
        // em vez de depender do flush automatico (que nao estava desenhando o texto
        // no passe de block entity).
        VertexConsumerProvider.Immediate immediate =
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        this.textRenderer.draw(Text.literal(name), x, 0, 0xFFFFFFFF, false, matrix, immediate,
                TextRenderer.TextLayerType.NORMAL, 0, 15728880);

        immediate.draw(); // flush imediato

        matrices.pop();
    }
}
