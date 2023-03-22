package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class ButtonComponentState extends RotatableComponentState {

    private boolean powered;

    // TODO - this component parameter should go, this whole state system needs rethinking
    public ButtonComponentState(FlatDirection direction, boolean powered, Component component) {
        super(component, direction);
        this.powered = powered;
    }

    public ButtonComponentState(byte data, Component component) {
        super(component, data);
        this.setPowered(((data >> 3) & 0b1) != 0);
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | (this.isPowered() ? (1 << 3) : 0));
    }

    public boolean isPowered() {
        return powered;
    }

    public ButtonComponentState setPowered(boolean powered) {
        this.powered = powered;
        return this;
    }
}
