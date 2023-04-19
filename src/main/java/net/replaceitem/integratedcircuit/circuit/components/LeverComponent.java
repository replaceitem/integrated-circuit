package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class LeverComponent extends FacingComponent {


    private static final BooleanComponentProperty POWERED = new BooleanComponentProperty("powered", 3);

    public LeverComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.lever"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/block/lever.png");

    public static final Identifier TEXTURE_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/lever_off.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/lever_on.png");

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(POWERED) ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        Identifier texture = state.get(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(matrices, texture, x, y, state.get(FACING).toInt(), 1, 1, 1, a);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        circuit.setComponentState(pos, state.cycle(POWERED), Block.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if(state.isOf(newState.getComponent())) return;
        if(state.get(POWERED)) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(POWERED);
    }
}
