package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.util.Identifier;

public class PortType {

    public final boolean isInput;
    public final boolean isAnalog;
    private final byte data;


    public static final PortType[] VALUES = new PortType[]{
            new PortType(0),
            new PortType(1),
            new PortType(2),
            new PortType(3)
    };

    public static final PortType DIGITAL_OUTPUT = VALUES[0];

    private PortType(int data) {
        this.isInput = (data & 0b1) != 0;
        this.isAnalog = (data & 0b10) != 0;
        this.data = (byte) data;
    }

    public static PortType fromData(byte data) {
        return VALUES[data];
    }

    public byte toData() {
        return data;
    }

    public Identifier getTexture() {
        return null;
    }

    public int getRotationOffset() {
        return isInput ? 2 : 0;
    }

    public PortType getNext() {
        return VALUES[(this.data+1)%4];
    }
}
