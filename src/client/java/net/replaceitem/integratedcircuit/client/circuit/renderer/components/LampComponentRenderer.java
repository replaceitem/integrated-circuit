package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.LampComponent.LIT;

public class LampComponentRenderer extends ComponentRenderer {
    public static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/lamp.png");
    public static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/lamp_on.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        Identifier texture = state.getValue(LIT) ? TEXTURE_ON : TEXTURE;
        ComponentRenderer.extractComponentTextureRenderState(graphics, texture, x, y, 0, a);
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return CircuitRenderer.getSignalStrengthText(state.getValue(LIT) ? 15 : 0);
    }
}
