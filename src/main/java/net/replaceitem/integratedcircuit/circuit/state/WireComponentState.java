package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.Direction;


public class WireComponentState extends ComponentState {
    private byte connections;
    private byte power;

    public WireComponentState(byte connections, byte power) {
        super(Components.WIRE);
        this.connections = connections;
        this.power = power;
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

    public boolean isConnected(Direction direction) {
        return (1 << direction.toInt() & this.connections) != 0;
    }

    public WireComponentState setConnected(Direction direction, boolean connected) {
        int mask = 1 << direction.toInt();
        this.connections = (byte) (connected ? connections | mask : connections & ~mask);
        return this;
    }

    public int getPower() {
        return power;
    }

    public WireComponentState setPower(byte power) {
        this.power = power;
        return this;
    }

    public WireComponentState setPower(int power) {
        return setPower((byte) power);
    }

    public byte getConnections() {
        return connections;
    }

}
