package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.Direction;

public class ObserverComponentState extends RotatableComponentState {
    
    private boolean powered;

    public ObserverComponentState(Direction direction, boolean powered) {
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
}
