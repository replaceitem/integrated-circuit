package net.replaceitem.integratedcircuit.circuit.state.property;

import net.replaceitem.integratedcircuit.util.FlatDirection;

public class FlatDirectionComponentProperty extends ComponentProperty<FlatDirection> {

    public FlatDirectionComponentProperty(String name, int bitPosition) {
        super(name, 2, bitPosition);
    }

    @Override
    public byte encodeBits(FlatDirection value) {
        return (byte) value.toInt();
    }

    @Override
    public FlatDirection decodeBits(byte data) {
        return FlatDirection.VALUES[data];
    }

    @Override
    public FlatDirection cycle(FlatDirection value) {
        return value.rotated(1);
    }
}
