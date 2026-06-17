package net.replaceitem.integratedcircuit.circuit.components;

import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public class BlockComponent extends Component {
    public BlockComponent(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return true;
    }
}
