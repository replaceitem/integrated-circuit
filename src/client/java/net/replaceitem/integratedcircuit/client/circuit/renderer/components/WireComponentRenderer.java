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
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.WireComponent.*;

public class WireComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE_DOT = IntegratedCircuit.id("textures/integrated_circuit/wire_dot.png");

    protected static final Identifier TEXTURE_X = IntegratedCircuit.id("textures/integrated_circuit/wire_x.png");
    protected static final Identifier TEXTURE_Y = IntegratedCircuit.id("textures/integrated_circuit/wire_y.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        final int size = CircuitRenderer.COMPONENT_SIZE;
        final int halfSize = size / 2;

        int color = ARGB.color(ARGB.as8BitChannel(a), RedStoneWireBlock.getColorForPower(state.getValue(POWER)));

        if(state.getValue(CONNECTED_NORTH)) ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_Y, x, y, 0, color, 0, 0, size, halfSize);
        if(state.getValue(CONNECTED_EAST)) ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_X, x, y, 0, color, halfSize, 0, halfSize, size);
        if(state.getValue(CONNECTED_SOUTH)) ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_Y, x, y, 0, color, 0, halfSize, size, halfSize);
        if(state.getValue(CONNECTED_WEST)) ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_X, x, y, 0, color, 0, 0, halfSize, size);

        int connections = 0;
        for (FlatDirection direction : FlatDirection.VALUES) if(state.getValue(DIRECTION_TO_CONNECTION_PROPERTY.get(direction))) connections++;
        if(connections != 2) ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_DOT, x, y, 0, color, 0, 0, size, size);

        if(!(state.getValue(CONNECTED_NORTH) && state.getValue(CONNECTED_SOUTH) || state.getValue(CONNECTED_EAST) && state.getValue(CONNECTED_WEST))) {
            ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_DOT, x, y, 0, color, 0, 0, size, size);
        }
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        int signalStrength = state.getValue(POWER);
        return CircuitRenderer.getSignalStrengthText(signalStrength);
    }
}
