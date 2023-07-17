package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class LampComponent extends Component {
    private static final BooleanComponentProperty LIT = new BooleanComponentProperty("lit", 0);

    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/lamp.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/lamp_on.png");

    public LampComponent(int id, Settings settings) {
        super(id, settings);
    }

    @Override
    public Identifier getItemTexture() {
        return TEXTURE;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        Identifier texture = state.get(LIT) ? TEXTURE_ON : TEXTURE;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, texture, x, y, 0, 1, 1, 1, a);
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(LIT) ? 15 : 0);
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        return this.getDefaultState().with(LIT, circuit.isReceivingRedstonePower(pos));
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (!circuit.isClient) {
            boolean bl = state.get(LIT);
            if (bl != circuit.isReceivingRedstonePower(pos)) {
                if (bl) {
                    circuit.scheduleBlockTick(pos, this, 4);
                } else {
                    circuit.setComponentState(pos, state.cycle(LIT), Component.NOTIFY_LISTENERS);
                }
            }

        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if (state.get(LIT) && !circuit.isReceivingRedstonePower(pos)) {
            circuit.setComponentState(pos, state.cycle(LIT), Component.NOTIFY_LISTENERS);
        }
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return true;
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(LIT);
    }
}
