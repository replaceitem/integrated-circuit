package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class AbstractRedstoneGateComponentState extends RotatableComponentState {
    
    protected boolean powered;


    public AbstractRedstoneGateComponentState(Component component, FlatDirection rotation, boolean powered) {
        super(component, rotation);
        this.powered = powered;
    }
    
    public AbstractRedstoneGateComponentState(Component component, byte data) {
        super(component, data);
        this.setPowered(((data >> 2) & 0b1) != 0);
    }


    public boolean isPowered() {
        return powered;
    }

    public AbstractRedstoneGateComponentState setPowered(boolean powered) {
        this.powered = powered;
        return this;
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | (this.isPowered()?(1<<2):0));
    }
}
