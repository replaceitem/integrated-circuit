package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;

public interface AbstractWireComponentState extends SignalStrengthAccessor {
    int getPower();

    AbstractWireComponentState setPower(int power);

    @Override
    default int getSignalStrength() {
        return getPower();
    }
}
