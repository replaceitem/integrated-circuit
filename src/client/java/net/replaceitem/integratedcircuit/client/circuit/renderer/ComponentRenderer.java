package net.replaceitem.integratedcircuit.client.circuit.renderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import org.jspecify.annotations.Nullable;

public abstract class ComponentRenderer {
    public abstract void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state);

    @Nullable
    public Component getHoverInfoText(ComponentState state) {
        return null;
    }


    public static void extractComponentTextureRenderState(GuiGraphicsExtractor graphics, Identifier component, int x, int y, int rot, float alpha) {
        extractComponentTextureRenderState(graphics, component, x, y, rot, ARGB.white(alpha));
    }

    public static void extractComponentTextureRenderState(GuiGraphicsExtractor graphics, Identifier component, int x, int y, int rot, int color) {
        extractComponentTextureRenderState(graphics, component, x, y, rot, color, 0, 0, 16, 16);
    }

    public static void extractComponentTextureRenderState(GuiGraphicsExtractor graphics, Identifier component, int x, int y, int rot, int color, int u, int v, int w, int h) {
        extractPartialTextureRenderState(graphics, component, x, y, u, v, 16, 16, rot, color, u, v, w, h);
    }

    public static void extractPartialTextureRenderState(GuiGraphicsExtractor graphics, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float alpha) {
        extractPartialTextureRenderState(graphics, texture, componentX, componentY, x, y, textureW, textureH, rot, ARGB.white(alpha));
    }

    public static void extractPartialTextureRenderState(GuiGraphicsExtractor graphics, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, int color) {
        extractPartialTextureRenderState(graphics, texture, componentX, componentY, x, y, textureW, textureH, rot, color, 0, 0, textureW, textureH);
    }

    private static void extractPartialTextureRenderState(GuiGraphicsExtractor graphics, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, int color, int u, int v, int w, int h) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(componentX + 8, componentY + 8);
        graphics.pose().rotate((float) (rot * Math.PI * 0.5));
        graphics.pose().translate(-8, -8);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, textureW, textureH, color);
        graphics.pose().popMatrix();
    }
}
