package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.FacingComponent.FACING;
import static net.replaceitem.integratedcircuit.circuit.components.LeverComponent.POWERED;

public class LeverComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/lever_off.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/lever_on.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        Identifier texture = state.getValue(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        ComponentRenderer.extractComponentTextureRenderState(graphics, texture, x, y, state.getValue(FACING).getIndex(), a);
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return CircuitRenderer.getSignalStrengthText(state.getValue(POWERED) ? 15 : 0);
    }
}
