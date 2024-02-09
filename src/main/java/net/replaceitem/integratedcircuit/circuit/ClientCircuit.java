package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.replaceitem.integratedcircuit.circuit.context.ClientCircuitContext;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class ClientCircuit extends Circuit {

    private final ClientCircuitContext context;
    
    public ClientCircuit(ClientCircuitContext context) {
        super(true);
        this.context = context;
    }

    public ClientCircuitContext getContext() {
        return context;
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return CircuitTickScheduler.getClientTickScheduler();
    }

    public static ClientCircuit fromNbt(NbtCompound nbt, ClientCircuitContext context) {
        if(nbt == null) return null;
        ClientCircuit circuit = new ClientCircuit(context);
        circuit.readNbt(nbt);
        return circuit;
    }

    public void onComponentUpdateFromServer(ComponentState state, ComponentPos pos) {
        this.setComponentState(pos, state, Component.NOTIFY_ALL | Component.FORCE_STATE);
    }

    @Override
    public void placeComponentState(ComponentPos pos, Component component, FlatDirection placementRotation) {
        new PlaceComponentC2SPacket(pos, this.context.getBlockPos(), component, placementRotation).send();
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
        new ComponentInteractionC2SPacket(pos, this.context.getBlockPos()).send();
        super.useComponent(pos, player);
    }

    @Override
    protected void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags) {

    }
}
