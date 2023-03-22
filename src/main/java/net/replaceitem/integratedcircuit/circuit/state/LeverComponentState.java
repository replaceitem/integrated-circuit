package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class LeverComponentState extends RotatableComponentState {

    private boolean powered;

    public LeverComponentState(FlatDirection direction, boolean powered) {
        super(Components.LEVER, direction);
        this.powered = powered;
    }

    public LeverComponentState(byte data) {
        super(Components.LEVER, data);
        this.setPowered(((data >> 3) & 0b1) != 0);
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | (this.isPowered() ? (1 << 3) : 0));
    }

    public boolean isPowered() {
        return powered;
    }

    public LeverComponentState setPowered(boolean powered) {
        this.powered = powered;
        return this;
    }
}
