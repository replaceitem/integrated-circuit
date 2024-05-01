package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.state.StateManager;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.FlatDirectionProperty;

public abstract class FacingComponent extends Component {

    public static final FlatDirectionProperty FACING = FlatDirectionProperty.of("facing");

    public FacingComponent(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}
