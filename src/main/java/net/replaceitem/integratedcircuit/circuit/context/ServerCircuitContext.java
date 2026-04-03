package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

public interface ServerCircuitContext extends CircuitContext {
    RandomSource getRandom();
    void onComponentUpdate(ComponentPos pos, ComponentState state);
    void playSound(@Nullable Player except, SoundEvent sound, SoundSource category, float volume, float pitch);
    void updateExternal(FlatDirection portSide);
    void readExternalPower(FlatDirection portSide);
    void markDirty();
    void setRenderStrength(FlatDirection portSide, int integer);
}
