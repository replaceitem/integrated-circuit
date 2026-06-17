package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;

import static net.replaceitem.integratedcircuit.circuit.components.CopperBulbComponent.LIT;
import static net.replaceitem.integratedcircuit.circuit.components.CopperBulbComponent.POWERED;

public class CopperBulbComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb.png");
    private static final Identifier TEXTURE_LIT = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_lit.png");
    private static final Identifier TEXTURE_POWERED = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_powered.png");
    private static final Identifier TEXTURE_LIT_POWERED = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_lit_powered.png");

    private Identifier getTexture(boolean lit, boolean powered) {
        return lit ? (powered ? TEXTURE_LIT_POWERED : TEXTURE_LIT) : (powered ? TEXTURE_POWERED : TEXTURE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        var texture = getTexture(state.getValue(LIT), state.getValue(POWERED));
        ComponentRenderer.extractComponentTextureRenderState(graphics, texture, x, y, 0, a);
    }
}
