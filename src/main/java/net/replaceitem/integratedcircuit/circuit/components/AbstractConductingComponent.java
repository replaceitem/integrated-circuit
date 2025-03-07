package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.HashSet;

public abstract class AbstractConductingComponent extends Component {
    public AbstractConductingComponent(Settings settings) {
        super(settings);
    }

    protected static final Identifier TEXTURE_X = IntegratedCircuit.id("textures/integrated_circuit/wire_x.png");
    protected static final Identifier TEXTURE_Y = IntegratedCircuit.id("textures/integrated_circuit/wire_y.png");


    protected static boolean wiresGivePower = true;


    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    protected abstract void update(Circuit circuit, ComponentPos pos, ComponentState state);

    protected void updateNeighbors(Circuit circuit, ComponentPos pos) {
        ComponentState componentState = circuit.getComponentState(pos);
        if (!(componentState.isOf(Components.WIRE) || componentState.isOf(Components.PORT) || componentState.isOf(Components.CROSSOVER))) {
            return;
        }
        circuit.updateNeighborsAlways(pos, this);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    protected void updateOffsetNeighbors(Circuit circuit, ComponentPos pos) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            this.updateNeighbors(circuit, pos.offset(direction));
        }
    }
    
    protected void updateAfterSignalStrengthChange(Circuit circuit, ComponentPos pos) {
        HashSet<ComponentPos> set = Sets.newHashSet();
        set.add(pos);
        for (FlatDirection direction : FlatDirection.VALUES) {
            set.add(pos.offset(direction));
        }
        for (ComponentPos blockPos : set) {
            circuit.updateNeighborsAlways(blockPos, this);
        }
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
}
