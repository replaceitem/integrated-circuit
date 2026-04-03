package net.replaceitem.integratedcircuit.circuit;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.replaceitem.integratedcircuit.circuit.context.ClientCircuitContext;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.RenameCircuitC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

public class ClientCircuit extends Circuit {

    private final ClientCircuitContext context;
    
    public ClientCircuit(ClientCircuitContext context) {
        super(true);
        this.context = context;
    }

    public ClientCircuit(ClientCircuitContext context, ComponentState[] portStates, CircuitSection section) {
        super(true, portStates, section);
        this.context = context;
    }

    public ClientCircuitContext getContext() {
        return context;
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return CircuitTickScheduler.getClientTickScheduler();
    }

    public void onComponentUpdateFromServer(ComponentState state, ComponentPos pos) {
        this.setComponentState(pos, state, Component.NOTIFY_ALL | Component.FORCE_STATE);
    }

    @Override
    public void placeComponentState(ComponentPos pos, Component component, FlatDirection placementRotation) {
        ClientPlayNetworking.send(new PlaceComponentC2SPacket(pos, this.context.getBlockPos(), component, placementRotation));
        ComponentState placementState = component.getPlacementState(this, pos, placementRotation);
        if(placementState != null) {
            boolean breaking = component == Components.AIR;
            SoundType soundGroup = (breaking ? getComponentState(pos).getComponent() : component).getSettings().soundGroup;
            boolean success = setComponentState(pos, placementState, Component.NOTIFY_ALL);
            if (success) {
                playSound(Minecraft.getInstance().player, breaking ? soundGroup.getBreakSound() : soundGroup.getPlaceSound(), SoundSource.BLOCKS, (soundGroup.getVolume() + 1.0f) / 2.0f, soundGroup.getPitch());
            }
        }
    }

    @Override
    public void playSoundInternal(@Nullable Player except, SoundEvent sound, SoundSource category, float volume, float pitch) {
        this.context.playSound(except, sound, category, volume, pitch);
    }

    public void breakComponentState(ComponentPos pos) {
        this.placeComponentState(pos, Components.AIR, FlatDirection.NORTH);
    }

    @Override
    public void useComponent(ComponentPos pos, Player player) {
        ClientPlayNetworking.send(new ComponentInteractionC2SPacket(pos, this.context.getBlockPos()));
        super.useComponent(pos, player);
    }

    public void rename(net.minecraft.network.chat.Component newName) {
        ClientPlayNetworking.send(new RenameCircuitC2SPacket(newName, this.context.getBlockPos()));
    }

    @Override
    public long getTime() {
        return context.getTime();
    }

    @Override
    protected void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags) {

    }
}
