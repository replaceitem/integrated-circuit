package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public class LecternComponent extends Component {
    public static final IntegerProperty PAGE = IntegerProperty.create("page", 1, 15);
    
    public LecternComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(PAGE, 1));
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        if(circuit.isClient) {
            return;
        }
        state = state.cycle(PAGE);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        circuit.playSound(null, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, 1);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        super.onStateReplaced(state, circuit, pos, newState);
        if(!newState.isOf(this) || !newState.getValue(PAGE).equals(state.getValue(PAGE))) {
            circuit.updateComparators(pos, this);
        }
    }

    @Override
    public boolean hasComparatorOutput(ComponentState componentState) {
        return true;
    }

    @Override
    public int getComparatorOutput(ComponentState state, Circuit circuit, ComponentPos pos) {
        return state.getValue(PAGE);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(PAGE);
    }
}
