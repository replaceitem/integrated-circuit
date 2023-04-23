package net.replaceitem.integratedcircuit.circuit.components;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.property.FlatDirectionComponentProperty;

public abstract class FacingComponent extends Component {

    public static final FlatDirectionComponentProperty FACING = new FlatDirectionComponentProperty("facing", 0);

    public FacingComponent(int id, Settings settings) {
        super(id, settings);
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(FACING);
    }
}
