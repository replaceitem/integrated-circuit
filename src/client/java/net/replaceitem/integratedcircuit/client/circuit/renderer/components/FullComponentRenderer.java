package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;

public class FullComponentRenderer extends ComponentRenderer {
    private final Identifier texture;

    public FullComponentRenderer(Identifier texture) {
        this.texture = texture;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        ComponentRenderer.extractComponentTextureRenderState(graphics, texture, x, y, 0, a);
    }
}
