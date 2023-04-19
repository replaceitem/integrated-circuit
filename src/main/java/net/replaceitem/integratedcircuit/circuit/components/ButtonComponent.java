package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class ButtonComponent extends FacingComponent {

    private static final BooleanComponentProperty POWERED = new BooleanComponentProperty("powered", 3);

    private final boolean wooden;

    public ButtonComponent(int id, boolean wooden) {
        // TODO move the translation to parameter (possibly using a settings builder class
        super(id, Text.translatable("component.integrated_circuit.button_" + (wooden ? "wood" : "stone")));
        this.wooden = wooden;
    }

    public static final Identifier TEXTURE_STONE = new IntegratedCircuitIdentifier("textures/integrated_circuit/button_stone.png");
    public static final Identifier TEXTURE_WOOD = new IntegratedCircuitIdentifier("textures/integrated_circuit/button_wood.png");

    @Override
    public Identifier getItemTexture() {
        return wooden ? TEXTURE_WOOD : TEXTURE_STONE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(POWERED) ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        Identifier texture = getItemTexture();
        float b = state.get(POWERED) ? 0.5f : 1f;
        IntegratedCircuitScreen.renderComponentTexture(matrices, texture, x, y, state.get(FACING).toInt(), b, b, b, a);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(state.get(POWERED)) return;
        powerOn(state, circuit, pos);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if(state.isOf(newState.getComponent())) return;
        if(state.get(POWERED)) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if(!state.get(POWERED)) return;
        circuit.setComponentState(pos, state.with(POWERED, false), Block.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
    }

    public void powerOn(ComponentState state, Circuit circuit, ComponentPos pos) {
        circuit.setComponentState(pos, state.with(POWERED, true), Block.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        circuit.scheduleBlockTick(pos, this, wooden ? 30 : 20);
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
