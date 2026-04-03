package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface ClientCircuitContext extends CircuitContext {
    BlockPos getBlockPos();
    void playSound(@Nullable Player except, SoundEvent sound, SoundSource category, float volume, float pitch);
}
