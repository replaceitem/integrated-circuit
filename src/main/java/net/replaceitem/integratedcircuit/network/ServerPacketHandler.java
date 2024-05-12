package net.replaceitem.integratedcircuit.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;

public class ServerPacketHandler {
    public static void receiveComponentInteraction(ComponentInteractionC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();
        if(
                world.getBlockState(packet.blockPos()).isIn(IntegratedCircuit.INTEGRATED_CIRCUITS_BLOCK_TAG) &&
                world.getBlockEntity(packet.blockPos()) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity &&
                integratedCircuitBlockEntity.getCircuit() != null
        ) {
            if(!integratedCircuitBlockEntity.isEditing(player)) return;
            integratedCircuitBlockEntity.getCircuit().useComponent(packet.pos(), player);
        }
    }

    public static void receiveFinishEditingPacket(FinishEditingC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        player.updateLastActionTime();
        ServerWorld serverWorld = player.getServerWorld();
        BlockEntity blockEntity = serverWorld.getBlockEntity(packet.pos());
        if (!(blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)) return;
        integratedCircuitBlockEntity.removeEditor(player);
    }

    public static void receivePlaceComponentPacket(PlaceComponentC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();
        if(
                world.getBlockState(packet.blockPos()).isIn(IntegratedCircuit.INTEGRATED_CIRCUITS_BLOCK_TAG) && 
                world.getBlockEntity(packet.blockPos()) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity &&
                integratedCircuitBlockEntity.getCircuit() != null
        ) {
            if(!integratedCircuitBlockEntity.isEditing(player)) return;
            integratedCircuitBlockEntity.getCircuit().placeComponentState(packet.pos(), packet.component(), packet.rotation());
        }
    }
}
