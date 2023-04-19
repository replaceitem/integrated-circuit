package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.*;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.FlatDirectionComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.IntComponentProperty;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class PortComponent extends AbstractWireComponent {

    public static final FlatDirectionComponentProperty FACING = new FlatDirectionComponentProperty("facing", 0);
    public static final IntComponentProperty POWER = new IntComponentProperty("power", 3, 4);
    private static final BooleanComponentProperty IS_OUTPUT = new BooleanComponentProperty("is_output", 7);

    public PortComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.port"));
    }

    private static final Identifier TEXTURE_ARROW = new IntegratedCircuitIdentifier("textures/integrated_circuit/port.png");

    @Override
    public Identifier getItemTexture() {
        return null;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        Vec3d color = RedstoneWireBlockAccessor.getCOLORS()[state.get(POWER)];
        float r = (float) color.x;
        float g = (float) color.y;
        float b = (float) color.z;

        FlatDirection rotation = state.get(FACING);
        IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_ARROW, x, y, rotation.toInt(), r, g, b, a);
        
        Identifier wireTexture = rotation.getAxis() == FlatDirection.Axis.X ? TEXTURE_X : TEXTURE_Y;
        IntegratedCircuitScreen.renderComponentTexture(matrices, wireTexture, x, y, 0, r, g, b, a);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if(circuit.isClient) return;
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        state = state.with(FACING, state.get(FACING).getOpposite()).cycle(IS_OUTPUT);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
    }

    @Override
    protected int getReceivedRedstonePower(Circuit world, ComponentPos pos) {
        ComponentState state = world.getComponentState(pos);
        if(!state.get(IS_OUTPUT)) return state.get(POWER);
        return super.getReceivedRedstonePower(world, pos);
    }

    public int getInternalPower(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if(!state.get(IS_OUTPUT)) return 0;
        return state.get(POWER);
    }

    public void assignExternalPower(ServerCircuit circuit, ComponentPos pos, ComponentState state, int newPower) {
        if(state.get(IS_OUTPUT)) return;
        if(state.get(POWER) == newPower) return;
        state = state.with(POWER, newPower);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!wiresGivePower) return 0;
        return state.get(FACING).getOpposite() == direction ? state.get(POWER) : 0;
    }

    @Override
    protected ComponentProperty<Integer> getPowerProperty() {
        return PortComponent.POWER;
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return state.get(POWER);
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(FACING);
        builder.append(POWER);
        builder.append(IS_OUTPUT);
    }
}
