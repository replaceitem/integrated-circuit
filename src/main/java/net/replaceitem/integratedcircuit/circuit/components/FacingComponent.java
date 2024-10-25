package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public abstract class FacingComponent extends Component {

    public static final EnumProperty<FlatDirection> FACING = EnumProperty.of("facing", FlatDirection.class);

    public FacingComponent(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }
}
