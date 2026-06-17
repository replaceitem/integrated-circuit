package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.AbstractRedstoneGateComponent.POWERED;
import static net.replaceitem.integratedcircuit.circuit.components.ComparatorComponent.MODE;
import static net.replaceitem.integratedcircuit.circuit.components.ComparatorComponent.OUTPUT_POWER;
import static net.replaceitem.integratedcircuit.circuit.components.FacingComponent.FACING;

public class ComparatorComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/comparator.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/comparator_on.png");
    private static final Identifier TEXTURE_TORCH_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_top_off.png");
    private static final Identifier TEXTURE_TORCH_ON = IntegratedCircuit.id("textures/integrated_circuit/torch_top_on.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        boolean powered = state.getValue(POWERED);
        int rot = state.getValue(FACING).getOpposite().getIndex();
        ComponentRenderer.extractComponentTextureRenderState(graphics, powered ? TEXTURE_ON : TEXTURE, x, y, rot, a);

        Identifier torchTexture = powered ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;

        ComponentRenderer.extractPartialTextureRenderState(graphics, torchTexture, x, y, 3, 10, 4, 4, rot, a);
        ComponentRenderer.extractPartialTextureRenderState(graphics, torchTexture, x, y, 9, 10, 4, 4, rot, a);

        Identifier modeTorchTexture = state.getValue(MODE) == ComparatorMode.SUBTRACT ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        ComponentRenderer.extractPartialTextureRenderState(graphics, modeTorchTexture, x, y, 6, 1, 4, 4, rot, a);
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        int signalStrength = state.getValue(OUTPUT_POWER);
        return CircuitRenderer.getSignalStrengthText(signalStrength);
    }
}
