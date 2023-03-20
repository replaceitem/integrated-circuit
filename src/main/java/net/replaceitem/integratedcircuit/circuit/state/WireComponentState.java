package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.FlatDirection;


public class WireComponentState extends ComponentState implements AbstractWireComponentState {
    private byte connections;
    protected byte power;
    public WireComponentState(int connections, int power) {
        super(Components.WIRE);
        this.connections = (byte) connections;
        this.power = (byte) power;
    }
    
    public WireComponentState(byte data) {
        super(Components.WIRE);
        this.connections = (byte) (data & 0xF);
        this.power = (byte) ((data >> 4) & 0xF);
    }

    @Override
    public byte encodeStateData() {
        return (byte) ((this.connections & 0xF) | (this.power & 0xF) << 4);
    }

    public boolean isConnected(FlatDirection direction) {
        return (1 << direction.toInt() & this.connections) != 0;
    }

    public WireComponentState setConnected(FlatDirection direction, boolean connected) {
        int mask = 1 << direction.toInt();
        this.connections = (byte) (connected ? connections | mask : connections & ~mask);
        return this;
    }

    public byte getConnections() {
        return connections;
    }

    public int getPower() {
        return power;
    }

    public WireComponentState setPower(int power) {
        this.power = (byte) power;
        return this;
    }
}
