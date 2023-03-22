package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.LeverComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class LeverComponent extends Component {
    public LeverComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.lever"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/block/lever.png");

    public static final Identifier TEXTURE_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/lever_off.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/lever_on.png");

    @Override
    public ComponentState getDefaultState() {
        return new LeverComponentState(FlatDirection.NORTH, false);
    }

    @Override
    public ComponentState getState(byte data) {
        return new LeverComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        if(!(state instanceof LeverComponentState leverComponentState)) throw new IllegalStateException("Invalid component state for component");
        return IntegratedCircuitScreen.getSignalStrengthText(leverComponentState.isPowered() ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof LeverComponentState leverComponentState)) throw new IllegalStateException("Invalid component state for component");
        Identifier texture = leverComponentState.isPowered() ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(matrices, texture, x, y, leverComponentState.getRotation().toInt(), 1, 1, 1, a);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(!(state instanceof LeverComponentState leverComponentState)) throw new IllegalStateException("Invalid component state for component");
        leverComponentState = ((LeverComponentState) state.copy()).setPowered(!leverComponentState.isPowered());
        circuit.setComponentState(pos, leverComponentState, Block.NOTIFY_ALL);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof LeverComponentState leverComponentState)) throw new IllegalStateException("Invalid component state for component");
        return leverComponentState.isPowered() ? 15 : 0;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }
}
