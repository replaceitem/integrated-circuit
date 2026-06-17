package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.CrossoverComponent.POWER_X;
import static net.replaceitem.integratedcircuit.circuit.components.CrossoverComponent.POWER_Y;

public class CrossoverComponentRenderer extends ComponentRenderer {
    protected static final Identifier TEXTURE_X = IntegratedCircuit.id("textures/integrated_circuit/wire_x.png");
    protected static final Identifier TEXTURE_Y = IntegratedCircuit.id("textures/integrated_circuit/wire_y.png");

    private static final Identifier TEXTURE_BRIDGE = IntegratedCircuit.id("textures/integrated_circuit/wire_bridge.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        int colorX = RedStoneWireBlock.getColorForPower(state.getValue(POWER_X));
        int colorY = RedStoneWireBlock.getColorForPower(state.getValue(POWER_Y));

        ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_X, x, y, 0, ARGB.color(ARGB.as8BitChannel(a), colorX));
        ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_BRIDGE, x, y, 0, a);
        ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_Y, x, y, 0, ARGB.color(ARGB.as8BitChannel(a), colorY));
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return Component.literal("─ ")
                .append(CircuitRenderer.getSignalStrengthText(state.getValue(POWER_X)))
                .append(" │ ")
                .append(CircuitRenderer.getSignalStrengthText(state.getValue(POWER_Y)));
    }
}
