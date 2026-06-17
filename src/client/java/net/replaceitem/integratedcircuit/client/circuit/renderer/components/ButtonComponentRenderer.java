package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.ButtonComponent.FACING;
import static net.replaceitem.integratedcircuit.circuit.components.ButtonComponent.POWERED;

public class ButtonComponentRenderer extends ComponentRenderer {
    private final Identifier texture;

    public ButtonComponentRenderer(Identifier texture) {
        this.texture = texture;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        float b = state.getValue(POWERED) ? 0.5f : 1f;
        ComponentRenderer.extractComponentTextureRenderState(graphics, texture, x, y, state.getValue(FACING).getIndex(), ARGB.colorFromFloat(a, b, b, b));
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return CircuitRenderer.getSignalStrengthText(state.getValue(POWERED) ? 15 : 0);
    }
}
