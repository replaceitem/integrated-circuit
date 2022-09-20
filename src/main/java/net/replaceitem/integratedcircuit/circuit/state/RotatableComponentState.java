package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.Direction;

public class RotatableComponentState extends ComponentState {

    protected Direction direction;

    public RotatableComponentState(Component component, Direction direction) {
        super(component);
        this.direction = direction;
    }

    public RotatableComponentState(Component component, byte data) {
        super(component);
        this.direction = Direction.VALUES[data & 0b11];
    }

    public Direction getRotation() {
        return direction;
    }

    public RotatableComponentState setRotation(Direction direction) {
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
