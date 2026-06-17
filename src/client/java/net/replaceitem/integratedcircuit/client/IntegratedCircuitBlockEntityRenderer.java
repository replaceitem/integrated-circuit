package net.replaceitem.integratedcircuit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.Vec3;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class IntegratedCircuitBlockEntityRenderer implements BlockEntityRenderer<IntegratedCircuitBlockEntity, IntegratedCircuitBlockEntityRenderState> {
    private final Font textRenderer;
    private static final float MAX_WIDTH = 7f/16; // black circuit box is 8 pixels, plus margin
    private static final float MAX_HEIGHT = 5f/16;
    
    public IntegratedCircuitBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.textRenderer = ctx.font();
    }

    @Override
    public IntegratedCircuitBlockEntityRenderState createRenderState() {
        return new IntegratedCircuitBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(IntegratedCircuitBlockEntity blockEntity, IntegratedCircuitBlockEntityRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.customName = DefaultConfig.getConfig().getRenderCircuitName() ? blockEntity.getCustomName() : null;
        state.orientation = blockEntity.getBlockState().getValue(IntegratedCircuitBlock.FACING).getRotation();
    }

    @Override
    public void submit(IntegratedCircuitBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        Component customName = state.customName;
        Quaternionf orientation = state.orientation;

        if (customName == null || orientation == null) return;

        matrices.pushPose();
        matrices.translate(0.5F, 3f/16+0.001f, 0.5F);
        matrices.mulPose(orientation);
        int light = state.lightCoords;
        int textWidth = textRenderer.width(customName);
        float scale = Math.min(MAX_WIDTH / textWidth, MAX_HEIGHT / textRenderer.lineHeight);
        matrices.scale(-scale, -scale, scale);
        queue.submitText(matrices, (float) -textWidth / 2, (float) -textRenderer.lineHeight /2, customName.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, light, CommonColors.WHITE, 0, 0);
        matrices.popPose();
    }
}
