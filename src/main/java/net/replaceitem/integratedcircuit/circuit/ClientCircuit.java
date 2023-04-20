package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class ClientCircuit extends Circuit {

    private final ClientWorld world;

    protected final BlockPos blockPos;

    public ClientCircuit(ClientWorld world, BlockPos blockPos) {
        super(true);
        this.world = world;
        this.blockPos = blockPos;
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return CircuitTickScheduler.getClientTickScheduler();
    }

    @Override
    public long getTime() {
        return this.world.getTime();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public static ClientCircuit fromNbt(NbtCompound nbt, ClientWorld world, BlockPos pos) {
        if(nbt == null) return null;
        ClientCircuit circuit = new ClientCircuit(world, pos);
        circuit.readNbt(nbt);
        return circuit;
    }

    public void onComponentUpdateFromServer(ComponentState state, ComponentPos pos) {
        this.setComponentState(pos, state, Component.NOTIFY_ALL | Component.FORCE_STATE);
    }

    @Override
    public void placeComponentState(ComponentPos pos, Component component, FlatDirection placementRotation) {
        new PlaceComponentC2SPacket(pos, this.blockPos, component, placementRotation).send();
        ComponentState placementState = component.getPlacementState(this, pos, placementRotation);
        boolean breaking = component == Components.AIR;
        BlockSoundGroup soundGroup = (breaking ? getComponentState(pos).getComponent() : component).getSettings().soundGroup;
        boolean success = setComponentState(pos, placementState, Component.NOTIFY_ALL);
        if(success) {
            playSound(MinecraftClient.getInstance().player, breaking ? soundGroup.getBreakSound() : soundGroup.getPlaceSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0f) / 2.0f, soundGroup.getPitch());
        }
    }

    public void breakComponentState(ComponentPos pos) {
        this.placeComponentState(pos, Components.AIR, FlatDirection.NORTH);
    }

    @Override
    public void useComponent(ComponentPos pos, PlayerEntity player) {
        new ComponentInteractionC2SPacket(pos, this.blockPos).send();
        super.useComponent(pos, player);
    }

    @Override
    protected void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags) {

    }

    @Override
    public void playSoundInWorld(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.world.playSound(except, this.blockPos, sound, category, volume, pitch);
    }
}
