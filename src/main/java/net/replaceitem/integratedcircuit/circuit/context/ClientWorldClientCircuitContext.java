package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ClientWorldClientCircuitContext implements ClientCircuitContext {
    private final ClientWorld world;
    private final BlockPos blockPos;

    public ClientWorldClientCircuitContext(ClientWorld world, BlockPos blockPos) {
        this.world = world;
        this.blockPos = blockPos;
    }


    @Override
    public void playSoundInWorld(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.world.playSound(except, this.blockPos, sound, category, volume, pitch);
    }

    @Override
    public BlockPos getBlockPos() {
        return null;
    }
}
