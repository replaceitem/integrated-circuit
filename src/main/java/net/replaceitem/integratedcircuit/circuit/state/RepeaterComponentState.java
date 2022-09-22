package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.Direction;
import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;

public class RepeaterComponentState extends AbstractRedstoneGateComponentState implements SignalStrengthAccessor {

    protected int delay;
    protected boolean locked;

    public RepeaterComponentState(Direction direction, boolean powered, int delay, boolean locked) {
        super(Components.REPEATER, direction, powered);
        this.delay = delay;
        this.locked = locked;
    }

    public RepeaterComponentState(byte data) {
        super(Components.REPEATER, data);
        this.setDelay((data >> 3) & 0b11);
        this.setLocked(((data >> 5) & 0b1) != 0);
        
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | ((this.getDelay() & 0b11) << 3) | (this.isLocked()?(1<<5):0));
    }

    public int getDelay() {
        return this.delay;
    }

    public RepeaterComponentState setDelay(int delay) {
        if(delay < 0 || delay >= 4) throw new IllegalArgumentException("delay must be between 0 and 4");
        this.delay = delay;
        return this;
    }

    public boolean isLocked() {
        return locked;
    }

    public RepeaterComponentState setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    public RepeaterComponentState cycleDelay() {
        return this.setDelay((this.delay + 1) % 4);
    }

    @Override
    public int getSignalStrength() {
        return powered ? 15 : 0;
    }
}
