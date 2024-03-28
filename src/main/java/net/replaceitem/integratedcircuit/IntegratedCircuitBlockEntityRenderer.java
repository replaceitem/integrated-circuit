package net.replaceitem.integratedcircuit;

import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

public class IntegratedCircuitBlockEntityRenderer implements BlockEntityRenderer<IntegratedCircuitBlockEntity> {
    private final TextRenderer textRenderer;
    private static final float MAX_WIDTH = 7f/16; // black circuit box is 8 pixels, plus margin
    private static final float MAX_HEIGHT = 5f/16;
    
    public IntegratedCircuitBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }
    
    @Override
    public void render(IntegratedCircuitBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState blockState = entity.getCachedState();
        Direction facing = blockState.get(IntegratedCircuitBlock.FACING);
        Text customName = entity.getCustomName();
        if (customName == null) return;
        
        matrices.push();
        matrices.translate(0.5F, 3f/16+0.001f, 0.5F);
        matrices.multiply(facing.getRotationQuaternion());
        int textWidth = textRenderer.getWidth(customName);
        float scale = Math.min(MAX_WIDTH / textWidth, MAX_HEIGHT / textRenderer.fontHeight);
        matrices.scale(-scale, -scale, scale);
        textRenderer.draw(customName, (float) -textWidth / 2, (float) -textRenderer.fontHeight /2, 0xFFFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        matrices.pop();
    }
}
