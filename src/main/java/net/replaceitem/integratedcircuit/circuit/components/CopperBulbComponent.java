package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public class CopperBulbComponent extends Component {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CopperBulbComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(LIT, false).setValue(POWERED, false));
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (oldState.getComponent() != state.getComponent() && circuit instanceof ServerCircuit serverCircuit) {
            update(state, serverCircuit, pos);
        }
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (circuit instanceof ServerCircuit serverCircuit) {
            update(state, serverCircuit, pos);
        }
    }

    public void update(ComponentState state, ServerCircuit circuit, ComponentPos pos) {
        boolean receivingPower = circuit.isReceivingRedstonePower(pos);
        if (receivingPower != state.getValue(POWERED)) {
            ComponentState newState = state;
            if (receivingPower) {
                newState = newState.cycle(LIT);
                circuit.playSound(null, newState.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 1, 1);
            }

            circuit.setComponentState(pos, newState.setValue(POWERED, receivingPower), NOTIFY_ALL);
        }
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return true;
    }

    @Override
    public boolean hasComparatorOutput(ComponentState componentState) {
        return true;
    }

    @Override
    public int getComparatorOutput(ComponentState state, Circuit circuit, ComponentPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(LIT, POWERED);
    }
}
