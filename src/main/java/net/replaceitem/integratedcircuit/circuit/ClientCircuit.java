package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class ClientCircuit extends Circuit {

    private final ClientWorld world;

    public ClientCircuit(ClientWorld world) {
        super(true);
        this.world = world;
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return CircuitTickScheduler.getClientTickScheduler();
    }

    @Override
    public long getTime() {
        return this.world.getTime();
    }

    public static ClientCircuit fromNbt(NbtCompound nbt, ClientWorld world) {
        if(nbt == null) return null;
        ClientCircuit circuit = new ClientCircuit(world);
        circuit.readNbt(nbt);
        return circuit;
    }

    public void onComponentUpdateFromServer(ComponentState state, ComponentPos pos) {
        this.setComponentState(pos, state, Component.NOTIFY_ALL | Component.FORCE_STATE);
    }

    public void placeComponentState(ComponentPos pos, Component component, FlatDirection placementRotation, BlockPos blockPos) {
        new PlaceComponentC2SPacket(pos, blockPos, component, placementRotation).send();
        ComponentState placementState = component.getPlacementState(this, pos, placementRotation);
        setComponentState(pos, placementState, Component.NOTIFY_ALL);
    }

    public void breakComponentState(ComponentPos pos, BlockPos blockPos) {
        this.placeComponentState(pos, Components.AIR, FlatDirection.NORTH, blockPos);
    }

    @Override
    public void useComponent(ComponentPos pos, BlockPos blockPos) {
        new ComponentInteractionC2SPacket(pos, blockPos).send();
        super.useComponent(pos, blockPos);
    }

    @Override
    protected void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags) {

    }
}
