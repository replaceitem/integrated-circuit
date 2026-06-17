package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public abstract class AbstractWireComponent extends AbstractConductingComponent {
    public AbstractWireComponent(Settings settings) {
        super(settings);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (oldState.getComponent() == state.getComponent() || circuit.isClient) {
            return;
        }
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if(circuit.isClient) return;
        update(circuit, pos, state);
    }



    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.getWeakRedstonePower(state, circuit, pos, direction);
    }


    @Override
    protected void update(Circuit circuit, ComponentPos pos, ComponentState state) {
        int i = getReceivedRedstonePower(circuit, pos);
        if (state.getValue(getPowerProperty()) != i) {
            if (circuit.getComponentState(pos) == state) {
                circuit.setComponentState(pos, state.setValue(getPowerProperty(), i), Component.NOTIFY_LISTENERS);
            }
            this.updateAfterSignalStrengthChange(circuit, pos);
        }
    }

    protected IntegerProperty getPowerProperty() {
        return WireComponent.POWER;
    }

    protected int getReceivedRedstonePower(Circuit circuit, ComponentPos pos) {
        wiresGivePower = false;
        int i = circuit.getReceivedRedstonePower(pos);
        wiresGivePower = true;
        int j = 0;
        if (i < 15) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                ComponentPos blockPos = pos.offset(direction);
                ComponentState blockState = circuit.getComponentState(blockPos);
                j = Math.max(j, blockState.increasePower(direction.getOpposite()));
            }
        }
        return Math.max(i, j - 1);
    }
}
