package net.replaceitem.integratedcircuit.circuit.state.property;

public class BooleanComponentProperty extends ComponentProperty<Boolean> {
    public BooleanComponentProperty(String name, int bitPosition) {
        super(name, 1, bitPosition);
    }

    @Override
    public byte encodeBits(Boolean value) {
        return (byte) (value ? 1 : 0);
    }

    @Override
    public Boolean decodeBits(byte data) {
        return data != 0;
    }

    @Override
    public Boolean cycle(Boolean value) {
        return !value;
    }
}
