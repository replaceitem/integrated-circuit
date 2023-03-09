package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class RotatableComponentState extends ComponentState {

    protected FlatDirection direction;

    public RotatableComponentState(Component component, FlatDirection direction) {
        super(component);
        this.direction = direction;
    }

    public RotatableComponentState(Component component, byte data) {
        super(component);
        this.direction = FlatDirection.VALUES[data & 0b11];
    }

    public FlatDirection getRotation() {
        return direction;
    }

    public RotatableComponentState setRotation(FlatDirection direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public byte encodeStateData() {
        return (byte) (this.getRotation().toInt() & 0b11);
    }

    public void rotate(int amount) {
        this.direction = this.direction.rotated(amount);
    }
}
