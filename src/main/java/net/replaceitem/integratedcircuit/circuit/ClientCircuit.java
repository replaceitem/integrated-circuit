package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class ClientCircuit extends Circuit {
    
    public ClientCircuit() {
        super();
    }

    public static ClientCircuit fromNbt(NbtCompound nbt) {
        if(nbt == null) return null;
        ClientCircuit circuit = new ClientCircuit();
        circuit.readNbt(nbt);
        return circuit;
    }

    public void onComponentUpdateFromServer(ComponentState state, ComponentPos pos) {
        if(isPort(pos) && state instanceof PortComponentState portComponentState) {
            this.setPortComponentState(pos, portComponentState, Component.NOTIFY_ALL | Component.FORCE_STATE);
        } else {
            this.setComponentState(pos, state, Component.NOTIFY_ALL | Component.FORCE_STATE);
        }
    }

    public void placeComponentState(ComponentPos pos, Component component, FlatDirection cursorRotation, BlockPos blockPos) {
        new PlaceComponentC2SPacket(pos, blockPos, component, cursorRotation).send();
    }

    public void breakComponentState(ComponentPos pos, BlockPos blockPos) {
        this.placeComponentState(pos, Components.AIR, FlatDirection.NORTH, blockPos);
    }

    public void cycleState(ComponentPos pos, BlockPos blockPos) {
        new ComponentInteractionC2SPacket(pos, blockPos).send();
    }
}
