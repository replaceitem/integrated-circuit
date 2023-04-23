package net.replaceitem.integratedcircuit.circuit.state.property;

import net.replaceitem.integratedcircuit.circuit.PropertyMap;

public abstract class ComponentProperty<T> {

    private final int bitPosition;
    protected final int mask;
    protected final String name;

    public ComponentProperty(String name, int bitCount, int bitPosition) {
        this.name = name;
        this.bitPosition = bitPosition;
        mask = ((1 << bitCount) - 1);
    }

    protected abstract byte encodeBits(T value);
    protected abstract T decodeBits(byte data);

    public void readIntoMap(PropertyMap map, byte data) {
        map.put(this, this.read(data));
    }

    public byte write(byte data, T value) {
        byte bits = encodeBits(value);
        data |= (bits & mask) << bitPosition;
        return data;
    }

    public T read(byte data) {
        return decodeBits((byte) ((data >> bitPosition) & mask));
    }

    public abstract T cycle(T value);

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
