package net.replaceitem.integratedcircuit.circuit.state.property;

public class IntComponentProperty extends ComponentProperty<Integer> {
    public IntComponentProperty(String name, int bitPosition, int bitCount) {
        super(name, bitCount, bitPosition);
    }

    @Override
    public byte encodeBits(Integer value) {
        return value.byteValue();
    }

    @Override
    public Integer decodeBits(byte data) {
        return (int) data;
    }

    @Override
    public Integer cycle(Integer value) {
        return (value+1) & mask;
    }
}
