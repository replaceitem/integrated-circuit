package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public interface ServerCircuitContext extends CircuitContext {
    Random getRandom();
    void onComponentUpdate(ComponentPos pos, ComponentState state);
    void playSound(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch);
    void updateExternal(FlatDirection portSide);
    void readExternalPower(FlatDirection portSide);
    void markDirty();
    void setRenderStrength(FlatDirection portSide, int integer);
}
