package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;

public class RedstoneBlockComponentState extends ComponentState implements SignalStrengthAccessor {
    public RedstoneBlockComponentState() {
        super(Components.REDSTONE_BLOCK);
    }
    
    @Override
    public byte encodeStateData() {
        return super.encodeStateData();
    }

    @Override
    public int getSignalStrength() {
        return 15;
    }
}
