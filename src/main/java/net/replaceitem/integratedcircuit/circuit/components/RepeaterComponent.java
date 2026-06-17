package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class RepeaterComponent extends AbstractRedstoneGateComponent {
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    public RepeaterComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(FACING, FlatDirection.NORTH).setValue(POWERED, false).setValue(DELAY, 1).setValue(LOCKED, false));
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        circuit.setComponentState(pos, state.cycle(DELAY), Block.UPDATE_ALL);
    }

    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return state.getValue(DELAY) * 2;
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState state = super.getPlacementState(circuit, pos, rotation);
        return state.setValue(LOCKED, this.isLocked(circuit, pos, state));
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (!circuit.isClient && direction.getAxis() != state.getValue(FACING).getAxis()) {
            return state.setValue(LOCKED, this.isLocked(circuit, pos, state));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    @Override
    public boolean isLocked(Circuit circuit, ComponentPos pos, ComponentState state) {
        return this.getMaxInputLevelSides(circuit, pos, state) > 0;
    }

    @Override
    protected boolean isValidInput(ComponentState state) {
        return isRedstoneGate(state);
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(DELAY, LOCKED);
    }
}
