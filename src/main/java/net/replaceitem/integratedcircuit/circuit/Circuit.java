package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.Direction;

import java.util.Arrays;

public abstract class Circuit {
    public static final int SIZE = 15;
    
    public static final ComponentPos[] PORTS_GRID_POS = new ComponentPos[]{
            new ComponentPos(7, -1),
            new ComponentPos(15, 7),
            new ComponentPos(7, 15),
            new ComponentPos(-1, 7)
    };

    public final ComponentState[][] components = new ComponentState[SIZE][SIZE];
    public final PortComponentState[] ports = new PortComponentState[4];


    public Circuit() {
        for (ComponentState[] componentState : components) {
            Arrays.fill(componentState, Components.AIR.getDefaultState());
        }
        for (int i = 0; i < ports.length; i++) {
            ports[i] = new PortComponentState(Direction.VALUES[i].getOpposite(), (byte) 0, false);
        }
    }

    public boolean isInside(ComponentPos pos) {
        return pos.getX() >= 0 && pos.getX() < SIZE && pos.getY() >= 0 && pos.getY() < SIZE;
    }

    public ComponentState getComponentState(ComponentPos componentPos) {
        if(!isInside(componentPos)) {
            if(isPort(componentPos)) {
                return ports[getPortNumber(componentPos)];
            }
            return Components.AIR.getDefaultState();
        }
        return this.components[componentPos.getX()][componentPos.getY()];
    }

    public boolean setComponentState(ComponentPos pos, ComponentState state, int flags) {
        if(!isInside(pos)) return false;
        if(state == null) state = Components.AIR_DEFAULT_STATE;
        this.components[pos.getX()][pos.getY()] = state;
        return true;
    }

    public void setPortComponentState(ComponentPos pos, PortComponentState newState, int flags) {
        if(isPort(pos)) {
            ports[getPortNumber(pos)] = newState;
        }
    }

    public int getPortNumber(ComponentPos pos) {
        for (int i = 0; i < 4; i++) {
            if(PORTS_GRID_POS[i].equals(pos)) return i;
        }
        return -1;
    }

    public boolean isPort(ComponentPos pos) {
        return getPortNumber(pos) != -1;
    }

    public void writeNbt(NbtCompound nbt) {
        byte[] portBytes = new byte[4];
        for (int i = 0; i < ports.length; i++) {
            portBytes[i] = ports[i].encodeStateData();
        }
        nbt.putByteArray("ports", portBytes);

        //packing two shorts in an int
        int componentDataSize = SIZE*SIZE;
        int[] componentsData = new int[MathHelper.ceilDiv(componentDataSize, 2)];
        for (int i = 0; i < componentDataSize; i++) {
            int shift = (i%2==0)?16:0;
            componentsData[i/2] |= (components[i%SIZE][i/SIZE].encode() & 0xFFFF) << shift;
        }
        nbt.putIntArray("components", componentsData);
    }

    public void readNbt(NbtCompound nbt) {
        byte[] portBytes = nbt.getByteArray("ports");
        for (int i = 0; i < portBytes.length; i++) {
            ports[i] = (PortComponentState) Components.PORT.getState(portBytes[i]);
        }
        int[] componentData = nbt.getIntArray("components");
        int componentDataSize = SIZE*SIZE;
        if(componentData.length != MathHelper.ceilDiv(componentDataSize, 2)) throw new RuntimeException("Invalid componentData length received");
        for (int i = 0; i < componentDataSize; i++) {
            int shift = (i%2==0)?16:0;
            components[i % SIZE][i / SIZE] = Components.createComponentState((short) (componentData[i / 2] >> shift & 0xFFFF));
        }
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    public boolean breakBlock(ComponentPos pos) {
        ComponentState blockState = this.getComponentState(pos);
        if (blockState.isAir()) {
            return false;
        }
        return this.setComponentState(pos, Components.AIR_DEFAULT_STATE, Component.NOTIFY_ALL);
    }
}
