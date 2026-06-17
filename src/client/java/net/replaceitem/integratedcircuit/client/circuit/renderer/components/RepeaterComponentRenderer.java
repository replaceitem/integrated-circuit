package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.AbstractRedstoneGateComponent.POWERED;
import static net.replaceitem.integratedcircuit.circuit.components.FacingComponent.FACING;
import static net.replaceitem.integratedcircuit.circuit.components.RepeaterComponent.DELAY;
import static net.replaceitem.integratedcircuit.circuit.components.RepeaterComponent.LOCKED;

public class RepeaterComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/repeater_off.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/repeater_on.png");
    private static final Identifier TEXTURE_TORCH_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_top_off.png");
    private static final Identifier TEXTURE_TORCH_ON = IntegratedCircuit.id("textures/integrated_circuit/torch_top_on.png");
    private static final Identifier TEXTURE_BAR = IntegratedCircuit.id("textures/integrated_circuit/repeater_bar.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        FlatDirection renderedRotation = state.getValue(FACING).getOpposite();

        Identifier baseTexture = state.getValue(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        ComponentRenderer.extractComponentTextureRenderState(graphics, baseTexture, x, y, renderedRotation.getIndex(), a);


        Identifier torchTexture = state.getValue(POWERED) ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;

        ComponentRenderer.extractPartialTextureRenderState(graphics, torchTexture, x, y, 6, 1, 4, 4, renderedRotation.getIndex(), a);

        boolean locked = state.getValue(LOCKED);
        Identifier knobTexture = locked ? TEXTURE_BAR : torchTexture;
        int knobOffsetAmount = (state.getValue(DELAY) - 1) * 2;
        if(locked) {
            ComponentRenderer.extractPartialTextureRenderState(graphics, knobTexture, x, y, 2, 6 + knobOffsetAmount, 12, 2, renderedRotation.getIndex(), a);
        } else {
            ComponentRenderer.extractPartialTextureRenderState(graphics, knobTexture, x, y, 6, 5 + knobOffsetAmount, 4, 4, renderedRotation.getIndex(), a);
        }
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return CircuitRenderer.getSignalStrengthText(state.getValue(POWERED) ? 15 : 0);
    }
}
