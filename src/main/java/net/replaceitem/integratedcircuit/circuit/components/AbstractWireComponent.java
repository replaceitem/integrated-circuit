package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.text.Text;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.HashSet;

public abstract class AbstractWireComponent extends AbstractConductingComponent {
    public AbstractWireComponent(int id, Settings settings) {
        super(id, settings);
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        int signalStrength = state.get(getPowerProperty());
        return IntegratedCircuitScreen.getSignalStrengthText(signalStrength);
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
        if (state.get(getPowerProperty()) != i) {
            if (circuit.getComponentState(pos) == state) {
                circuit.setComponentState(pos, state.with(getPowerProperty(), i), Component.NOTIFY_LISTENERS);
            }
            HashSet<ComponentPos> set = Sets.newHashSet();
            set.add(pos);
            for (FlatDirection direction : FlatDirection.VALUES) {
                set.add(pos.offset(direction));
            }
            for (ComponentPos blockPos : set) {
                circuit.updateNeighborsAlways(blockPos, this);
            }
        }
    }

    protected ComponentProperty<Integer> getPowerProperty() {
        return WireComponent.POWER;
    }

    protected int getReceivedRedstonePower(Circuit world, ComponentPos pos) {
        wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        wiresGivePower = true;
        int j = 0;
        if (i < 15) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                ComponentPos blockPos = pos.offset(direction);
                ComponentState blockState = world.getComponentState(blockPos);
                j = Math.max(j, blockState.increasePower(direction.getOpposite()));
            }
        }
        return Math.max(i, j - 1);
    }
}
