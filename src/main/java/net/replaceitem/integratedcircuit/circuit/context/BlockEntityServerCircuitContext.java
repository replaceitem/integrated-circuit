package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BlockEntityServerCircuitContext implements ServerCircuitContext {
    
    private final IntegratedCircuitBlockEntity blockEntity;

    public BlockEntityServerCircuitContext(IntegratedCircuitBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
    
    private @Nullable World getWorld() {
        return this.blockEntity.getWorld();
    }
    
    private BlockPos getPos() {
        return this.blockEntity.getPos();
    }

    @Override
    public Random getRandom() {
        World world = getWorld();
        if(world == null) return Random.create();
        return world.getRandom();
    }

    @Override
    public boolean isReady() {
        return this.blockEntity.hasWorld();
    }

    @Override
    public void markDirty() {
        World world = getWorld();
        blockEntity.markDirty();
        if(world != null) world.markDirty(getPos());
    }

    @Override
    public void setRenderStrength(FlatDirection portSide, int power) {
        this.blockEntity.setRenderSignalStrength(portSide, power);
        markDirty();
        World world = getWorld();
        if(world != null) {
            BlockPos pos = getPos();
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void onComponentUpdate(ComponentPos pos, ComponentState state) {
        Set<ServerPlayerEntity> editors = this.blockEntity.getEditingPlayers();
        new ComponentUpdateS2CPacket(pos, state).send(editors);
    }

    @Override
    public void updateExternal(FlatDirection portSide) {
        World world = getWorld();
        if(world == null) return;
        BlockPos pos = getPos();
        BlockState state = world.getBlockState(pos);
        Direction vanillaDirection = portSide.toVanillaDirection(state);
        if(state.getBlock() instanceof IntegratedCircuitBlock integratedCircuitBlock) {
            integratedCircuitBlock.updateTarget(world, pos, vanillaDirection);
        }
    }

    @Override
    public void readExternalPower(FlatDirection direction) {
        World world = getWorld();
        if(world == null) return;
        BlockPos pos = getPos();
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof IntegratedCircuitBlock integratedCircuitBlock && blockEntity.getCircuit() != null) {
            int power = integratedCircuitBlock.getInputPower(world, pos, state, direction);
            blockEntity.getCircuit().onExternalPowerChanged(direction, power);
        }
    }

    @Override
    public void playSound(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if(getWorld() != null) {
            for (ServerPlayerEntity editingPlayer : this.blockEntity.getEditingPlayers()) {
                if(editingPlayer.equals(except)) continue;
                Vec3d soundPos = getPos().toCenterPos();
                editingPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(sound), category, soundPos.x, soundPos.y, soundPos.z, volume, pitch, this.getRandom().nextLong()));
            }
        }
    }

    @Override
    public long getTime() {
        World world = getWorld();
        if(world == null) return 0;
        return world.getTime();
    }
}
