package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class PortComponent extends AbstractWireComponent {
    private static final Identifier TEXTURE_ARROW = IntegratedCircuit.id("textures/integrated_circuit/port.png");

    public static final EnumProperty<FlatDirection> FACING = FacingComponent.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty IS_OUTPUT = BooleanProperty.create("is_output");

    public PortComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(FACING, FlatDirection.NORTH).setValue(POWER, 0).setValue(IS_OUTPUT, false));
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (circuit.isClient) return;
        ServerCircuitContext context = ((ServerCircuit) circuit).getContext();
        FlatDirection portSide = Circuit.getPortSide(pos);
        if(portSide == null) throw new IllegalStateException("Cannot place port on non-port location");
        boolean isOutput = state.getValue(IS_OUTPUT);
        boolean wasOutput = oldState.getValue(IS_OUTPUT);
        context.setRenderStrength(portSide, state.getValue(POWER));
        this.update(circuit, pos, state);
        this.updateAfterSignalStrengthChange(circuit, pos);
        this.updateOffsetNeighbors(circuit, pos);
        if (isOutput || wasOutput) context.updateExternal(portSide);
        if (!isOutput && wasOutput) context.readExternalPower(portSide);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        state = state.setValue(FACING, state.getValue(FACING).getOpposite()).cycle(IS_OUTPUT);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
    }

    @Override
    protected int getReceivedRedstonePower(Circuit circuit, ComponentPos pos) {
        ComponentState state = circuit.getComponentState(pos);
        if (!state.getValue(IS_OUTPUT)) return state.getValue(POWER);
        return super.getReceivedRedstonePower(circuit, pos);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (!wiresGivePower) return 0;
        return state.getValue(FACING).getOpposite() == direction ? state.getValue(POWER) : 0;
    }

    @Override
    protected IntegerProperty getPowerProperty() {
        return PortComponent.POWER;
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return state.getValue(POWER);
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
        builder.add(POWER);
        builder.add(IS_OUTPUT);
    }
}
