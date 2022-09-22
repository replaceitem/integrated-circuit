package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.Direction;
import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;

public class TorchComponentState extends RotatableComponentState implements SignalStrengthAccessor {

    private boolean lit;
    
    public TorchComponentState(Direction direction, boolean lit) {
        super(Components.TORCH, direction);
        this.lit = lit;
    }

    public TorchComponentState(byte data) {
        super(Components.TORCH, data);
        this.setLit(((data >> 2) & 0b1) != 0);
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | ((this.isLit() ? 1 : 0) << 2));
    }

    public boolean isLit() {
        return lit;
    }

    public TorchComponentState setLit(boolean lit) {
        this.lit = lit;
        return this;
    }

    @Override
    public int getSignalStrength() {
        return lit ? 15 : 0;
    }
}
