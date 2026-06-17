package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class LeverComponent extends FacingComponent {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LeverComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(FACING, FlatDirection.NORTH).setValue(POWERED, false));
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        if(circuit.isClient) {
            return;
        }
        state = this.togglePower(state, circuit, pos);
        float f = state.getValue(POWERED) ? 0.6f : 0.5f;
        circuit.playSound(null, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1, f);
    }

    public ComponentState togglePower(ComponentState state, Circuit circuit, ComponentPos pos) {
        state = state.cycle(POWERED);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        return state;
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if(state.isOf(newState.getComponent())) return;
        if(state.getValue(POWERED)) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.getValue(POWERED) ? 15 : 0;
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
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }
}
