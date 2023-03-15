package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.state.AbstractWireComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

import java.util.HashSet;

public abstract class AbstractWireComponent extends Component {
    public AbstractWireComponent(int id, Text name) {
        super(id, name);
    }

    protected static final Identifier TEXTURE_X = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_x.png");
    protected static final Identifier TEXTURE_Y = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_y.png");


    protected static boolean wiresGivePower = true;

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
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if (state.isOf(newState.getComponent())) {
            return;
        }
        super.onStateReplaced(state, circuit, pos, newState);
        if(circuit.isClient) return;
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
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
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.getWeakRedstonePower(state, circuit, pos, direction);
    }



    protected void update(Circuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof AbstractWireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        int i = getReceivedRedstonePower(circuit, pos);
        if (wireComponentState.getPower() != i) {
            if (circuit.getComponentState(pos).equals(state)) {
                AbstractWireComponentState newState = (AbstractWireComponentState) state.copy();
                newState.setPower(i);
                circuit.setComponentState(pos, (ComponentState) newState, Component.NOTIFY_LISTENERS);
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

    protected void updateOffsetNeighbors(Circuit circuit, ComponentPos pos) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            this.updateNeighbors(circuit, pos.offset(direction));
        }
    }

    protected void updateNeighbors(Circuit circuit, ComponentPos pos) {
        ComponentState componentState = circuit.getComponentState(pos);
        if (!(componentState.isOf(Components.WIRE) || componentState.isOf(Components.PORT))) {
            return;
        }
        circuit.updateNeighborsAlways(pos, this);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
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
                j = Math.max(j, increasePower(blockState));
            }
        }
        return Math.max(i, j - 1);
    }

    protected int increasePower(ComponentState blockState) {
        return blockState instanceof AbstractWireComponentState wireComponentState ? wireComponentState.getPower() : 0;
    }
}
