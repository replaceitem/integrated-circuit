package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class ClientWorldClientCircuitContext implements ClientCircuitContext {
    private final ClientLevel world;
    private final BlockPos blockPos;

    public ClientWorldClientCircuitContext(ClientLevel world, BlockPos blockPos) {
        this.world = world;
        this.blockPos = blockPos;
    }

    @Override
    public void playSound(@Nullable Player except, SoundEvent sound, SoundSource category, float volume, float pitch) {
        this.world.playSound(except, this.blockPos, sound, category, volume, pitch);
    }

    @Override
    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public long getTime() {
        return world.getGameTime();
    }
}
