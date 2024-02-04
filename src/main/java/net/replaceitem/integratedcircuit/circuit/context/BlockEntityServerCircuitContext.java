package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BlockEntityServerCircuitContext implements ServerCircuitContext {
    
    private final IntegratedCircuitBlockEntity blockEntity;

    public BlockEntityServerCircuitContext(IntegratedCircuitBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public Random getRandom() {
        return this.blockEntity.getWorld().getRandom();
    }

    @Override
    public boolean isReady() {
        return this.blockEntity.hasWorld();
    }

    @Override
    public void onComponentUpdate(ComponentPos pos, ComponentState state) {
        Set<ServerPlayerEntity> editors = this.blockEntity.getEditingPlayers();
        new ComponentUpdateS2CPacket(pos, state).send(editors);
    }

    @Override
    public BlockPos getBlockPos() {
        return blockEntity.getPos();
    }

    @Override
    public void playSoundInWorld(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if(this.blockEntity.getWorld() != null) {
            for (ServerPlayerEntity editingPlayer : this.blockEntity.getEditingPlayers()) {
                if(editingPlayer.equals(except)) continue;
                Vec3d soundPos = this.blockEntity.getPos().toCenterPos();
                editingPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(sound), category, soundPos.x, soundPos.y, soundPos.z, volume, pitch, this.getRandom().nextLong()));
            }
        }
    }
}
