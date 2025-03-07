package net.replaceitem.integratedcircuit.circuit;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.circuit.context.ClientCircuitContext;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.RenameCircuitC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

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
        boolean breaking = component == Components.AIR;
        BlockSoundGroup soundGroup = (breaking ? getComponentState(pos).getComponent() : component).getSettings().soundGroup;
        boolean success = setComponentState(pos, placementState, Component.NOTIFY_ALL);
        if(success) {
            playSound(MinecraftClient.getInstance().player, breaking ? soundGroup.getBreakSound() : soundGroup.getPlaceSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0f) / 2.0f, soundGroup.getPitch());
        }
    }

    @Override
    public void playSoundInternal(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.context.playSound(except, sound, category, volume, pitch);
    }

    public void breakComponentState(ComponentPos pos) {
        this.placeComponentState(pos, Components.AIR, FlatDirection.NORTH);
    }

    @Override
    public void useComponent(ComponentPos pos, PlayerEntity player) {
        ClientPlayNetworking.send(new ComponentInteractionC2SPacket(pos, this.context.getBlockPos()));
        super.useComponent(pos, player);
    }

    public void rename(Text newName) {
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
