package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;

public class ObserverComponentState extends RotatableComponentState implements SignalStrengthAccessor {
    
    private boolean powered;

    public ObserverComponentState(FlatDirection direction, boolean powered) {
        super(Components.OBSERVER, direction);
        this.powered = powered;
    }

    public ObserverComponentState(byte data) {
        super(Components.OBSERVER, data);
        this.setPowered(((data >> 3) & 0b1) != 0);
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | (this.isPowered() ? (1 << 3) : 0));
    }

    public boolean isPowered() {
        return powered;
    }

    public ObserverComponentState setPowered(boolean powered) {
        this.powered = powered;
        return this;
    }

    @Override
    public int getSignalStrength() {
        return powered ? 15 : 0;
    }
}
