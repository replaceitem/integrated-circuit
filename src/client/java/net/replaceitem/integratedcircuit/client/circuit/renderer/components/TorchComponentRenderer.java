package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.TorchComponent.FACING;
import static net.replaceitem.integratedcircuit.circuit.components.TorchComponent.LIT;

public class TorchComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/torch.png");
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_off.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        Identifier texture = state.getValue(LIT) ? TEXTURE : TEXTURE_OFF;
        ComponentRenderer.extractComponentTextureRenderState(graphics, texture, x, y, state.getValue(FACING).getIndex(), a);
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return CircuitRenderer.getSignalStrengthText(state.getValue(LIT) ? 15 : 0);
    }
}
