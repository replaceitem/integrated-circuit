package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.CircuitRenderer;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.PortComponent.*;

public class PortComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE_ARROW = IntegratedCircuit.id("textures/integrated_circuit/port.png");

    protected static final Identifier TEXTURE_X = IntegratedCircuit.id("textures/integrated_circuit/wire_x.png");
    protected static final Identifier TEXTURE_Y = IntegratedCircuit.id("textures/integrated_circuit/wire_y.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        int color = RedStoneWireBlock.getColorForPower(state.getValue(POWER));

        FlatDirection rotation = state.getValue(FACING);
        ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE_ARROW, x, y, rotation.getIndex(), color);

        Identifier wireTexture = rotation.getAxis() == FlatDirection.Axis.X ? TEXTURE_X : TEXTURE_Y;
        ComponentRenderer.extractComponentTextureRenderState(graphics, wireTexture, x, y, 0, color);
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        int signalStrength = state.getValue(POWER);

        return net.minecraft.network.chat.Component.translatable(
                        state.getValue(IS_OUTPUT)
                                ? "integrated_circuit.component.integrated_circuit.port_output"
                                : "integrated_circuit.component.integrated_circuit.port_input"
                )
                .append(" | ")
                .append(CircuitRenderer.getSignalStrengthText(signalStrength));
    }
}
