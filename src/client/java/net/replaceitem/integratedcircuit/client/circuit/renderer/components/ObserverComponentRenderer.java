package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.ObserverComponent.FACING;
import static net.replaceitem.integratedcircuit.circuit.components.ObserverComponent.POWERED;

public class ObserverComponentRenderer extends ComponentRenderer {
    public static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/observer.png");
    public static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/observer_on.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        ComponentRenderer.extractComponentTextureRenderState(graphics, state.getValue(POWERED) ? TEXTURE_ON : TEXTURE, x, y, state.getValue(FACING).getOpposite().getIndex(), a);
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return CircuitRenderer.getSignalStrengthText(state.getValue(POWERED) ? 15 : 0);
    }
}
