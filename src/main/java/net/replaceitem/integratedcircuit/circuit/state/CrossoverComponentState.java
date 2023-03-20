package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Components;

public class CrossoverComponentState extends ComponentState {
    protected byte powerX;
    protected byte powerY;
    public CrossoverComponentState(int powerX, int powerY) {
        super(Components.CROSSOVER);
        this.powerX = (byte) powerX;
        this.powerY = (byte) powerY;
    }

    public CrossoverComponentState(byte data) {
        super(Components.CROSSOVER);
        this.powerX = (byte) (data & 0xF);
        this.powerY = (byte) ((data >> 4) & 0xF);
    }

    @Override
    public byte encodeStateData() {
        return (byte) ((this.powerX & 0xF) | (this.powerY & 0xF) << 4);
    }

    public int getPowerX() {
        return powerX;
    }
    public int getPowerY() {
        return powerY;
    }

    public CrossoverComponentState setPowerX(int power) {
        this.powerX = (byte) power;
        return this;
    }
    public CrossoverComponentState setPowerY(int power) {
        this.powerY = (byte) power;
        return this;
    }
}
