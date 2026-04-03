package net.replaceitem.integratedcircuit.circuit.context;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class BlockEntityServerCircuitContext implements ServerCircuitContext {
    
    private final IntegratedCircuitBlockEntity blockEntity;

    public BlockEntityServerCircuitContext(IntegratedCircuitBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
    
    private @Nullable Level getWorld() {
        return this.blockEntity.getLevel();
    }
    
    private BlockPos getPos() {
        return this.blockEntity.getBlockPos();
    }

    @Override
    public RandomSource getRandom() {
        Level world = getWorld();
        if(world == null) return RandomSource.create();
        return world.getRandom();
    }
    
    @Override
    public void markDirty() {
        Level world = getWorld();
        blockEntity.setChanged();
        if(world != null) world.blockEntityChanged(getPos());
    }

    @Override
    public void setRenderStrength(FlatDirection portSide, int power) {
        this.blockEntity.setRenderSignalStrength(portSide, power);
        markDirty();
        Level world = getWorld();
        if(world != null) {
            BlockPos pos = getPos();
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onComponentUpdate(ComponentPos pos, ComponentState state) {
        Set<ServerPlayer> editors = this.blockEntity.getEditingPlayers();
        ComponentUpdateS2CPacket packet = new ComponentUpdateS2CPacket(pos, state);
        for (ServerPlayer player : editors) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    @Override
    public void updateExternal(FlatDirection portSide) {
        Level world = getWorld();
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
        Level world = getWorld();
        if(world == null) return;
        BlockPos pos = getPos();
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof IntegratedCircuitBlock integratedCircuitBlock && blockEntity.getCircuit() != null) {
            int power = integratedCircuitBlock.getInputPower(world, pos, state, direction);
            blockEntity.getCircuit().onExternalPowerChanged(direction, power);
        }
    }

    @Override
    public void playSound(@Nullable Player except, SoundEvent sound, SoundSource category, float volume, float pitch) {
        if(getWorld() != null) {
            for (ServerPlayer editingPlayer : this.blockEntity.getEditingPlayers()) {
                if(editingPlayer.equals(except)) continue;
                Vec3 soundPos = getPos().getCenter();
                editingPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), category, soundPos.x, soundPos.y, soundPos.z, volume, pitch, this.getRandom().nextLong()));
            }
        }
    }

    @Override
    public long getTime() {
        Level world = getWorld();
        if(world == null) return 0;
        return world.getGameTime();
    }
}
