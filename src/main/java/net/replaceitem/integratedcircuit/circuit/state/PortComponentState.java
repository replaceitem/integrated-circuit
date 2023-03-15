package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class PortComponentState extends RotatableComponentState implements AbstractWireComponentState {

    protected byte power;
    private boolean isOutput;

    public PortComponentState(FlatDirection direction, byte power, boolean isOutput) {
        super(Components.PORT, direction);
        this.power = power;
        this.isOutput = isOutput;
    }

    public PortComponentState(byte data) {
        super(Components.PORT, data);
        this.setPower((byte) ((data >> 3) & 0b1111));
        this.setOutput(((data >> 7) & 0b1) != 0);
    }

    @Override
    public byte encodeStateData() {
        return (byte) (super.encodeStateData() | ((this.getPower() & 0b1111) << 3) | (this.isOutput() ? (1 << 7) : 0));
    }

    public boolean isOutput() {
        return isOutput;
    }

    public PortComponentState setOutput(boolean output) {
        isOutput = output;
        return this;
    }
    
    
    public void assignExternalPower(ServerCircuit circuit, ComponentPos pos, int newPower) {
        ((PortComponent) this.component).assignExternalPower(circuit, pos, this, newPower);
    }
    
    public int getInternalPower(ServerCircuit circuit, ComponentPos pos) {
        return ((PortComponent) this.component).getInternalPower(circuit, pos, this);
    }

    public int getPower() {
        return power;
    }

    public PortComponentState setPower(int power) {
        this.power = (byte) power;
        return this;
    }
}
