package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.Direction;
import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;

public class ComparatorComponentState extends AbstractRedstoneGateComponentState implements SignalStrengthAccessor {

    protected boolean subtractMode;
    protected byte outputSignal;

    public ComparatorComponentState(Direction direction, boolean powered, boolean subtractMode) {
        super(Components.COMPARATOR, direction, powered);
        this.subtractMode = subtractMode;
    }

    public ComparatorComponentState(byte data) {
        super(Components.COMPARATOR, data);
        this.setSubtractMode(((data >> 3) & 0b1) != 0);
        this.setOutputSignal((data >> 4) & 0b1111);
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | ((this.isSubtractMode() ? 1 : 0) << 3) | ((this.getOutputSignal() & 0b1111) << 4));
    }

    public boolean isSubtractMode() {
        return this.subtractMode;
    }

    public ComparatorComponentState setSubtractMode(boolean subtractMode) {
        this.subtractMode = subtractMode;
        return this;
    }
    
    public byte getOutputSignal() {
        return outputSignal;
    }

    public void setOutputSignal(byte outputSignal) {
        this.outputSignal = outputSignal;
    }
    public void setOutputSignal(int outputSignal) {
        setOutputSignal((byte) outputSignal);
    }

    @Override
    public int getSignalStrength() {
        return outputSignal;
    }
}
