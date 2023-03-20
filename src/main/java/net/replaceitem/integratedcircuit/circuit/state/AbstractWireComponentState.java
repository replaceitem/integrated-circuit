package net.replaceitem.integratedcircuit.circuit.state;

public interface AbstractWireComponentState {
    int getPower();

    AbstractWireComponentState setPower(int power);
}
