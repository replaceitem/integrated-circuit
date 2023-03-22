package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.ButtonComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class ButtonComponent extends Component {

    private final boolean wooden;

    public ButtonComponent(int id, boolean wooden) {
        super(id, Text.translatable("component.integrated_circuit.button_" + (wooden ? "wood" : "stone")));
        this.wooden = wooden;
    }

    public static final Identifier TEXTURE_STONE = new IntegratedCircuitIdentifier("textures/integrated_circuit/button_stone.png");
    public static final Identifier TEXTURE_WOOD = new IntegratedCircuitIdentifier("textures/integrated_circuit/button_wood.png");

    @Override
    public ComponentState getDefaultState() {
        return new ButtonComponentState(FlatDirection.NORTH, false, this);
    }

    @Override
    public ComponentState getState(byte data) {
        return new ButtonComponentState(data, this);
    }

    @Override
    public Identifier getItemTexture() {
        return wooden ? TEXTURE_WOOD : TEXTURE_STONE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        if(!(state instanceof ButtonComponentState buttonComponentState)) throw new IllegalStateException("Invalid component state for component");
        return IntegratedCircuitScreen.getSignalStrengthText(buttonComponentState.isPowered() ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof ButtonComponentState buttonComponentState)) throw new IllegalStateException("Invalid component state for component");
        Identifier texture = getItemTexture();
        float b = buttonComponentState.isPowered() ? 0.5f : 1f;
        IntegratedCircuitScreen.renderComponentTexture(matrices, texture, x, y, buttonComponentState.getRotation().toInt(), b, b, b, a);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(!(state instanceof ButtonComponentState buttonComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(buttonComponentState.isPowered()) return;
        powerOn(state, circuit, pos);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if(!(state instanceof ButtonComponentState buttonComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(state.isOf(newState.getComponent())) return;
        if(buttonComponentState.isPowered()) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if(!(state instanceof ButtonComponentState buttonComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(!buttonComponentState.isPowered()) return;

        buttonComponentState = ((ButtonComponentState) buttonComponentState.copy()).setPowered(false);
        circuit.setComponentState(pos, buttonComponentState, Block.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
    }

    public void powerOn(ComponentState state, Circuit circuit, ComponentPos pos) {
        ButtonComponentState buttonComponentState = ((ButtonComponentState) state.copy()).setPowered(true);
        circuit.setComponentState(pos, buttonComponentState, Block.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        circuit.scheduleBlockTick(pos, this, wooden ? 30 : 20);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof ButtonComponentState buttonComponentState)) throw new IllegalStateException("Invalid component state for component");
        return buttonComponentState.isPowered() ? 15 : 0;
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
