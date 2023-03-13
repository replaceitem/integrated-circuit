package net.replaceitem.integratedcircuit.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;

public class ServerPacketHandler {
    public static void receiveFinishEditingPacket(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        FinishEditingC2SPacket packet = new FinishEditingC2SPacket(buf);
        minecraftServer.executeSync(() -> {
            serverPlayerEntity.updateLastActionTime();
            ServerWorld serverWorld = serverPlayerEntity.getWorld();
            BlockPos blockPos = packet.pos;
            if (serverWorld.isChunkLoaded(blockPos)) {
                BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
                if (!(blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)) return;
                if (!serverPlayerEntity.getUuid().equals(integratedCircuitBlockEntity.getEditor())) return;
                integratedCircuitBlockEntity.setEditor(null);
            }
        });
    }

    public static void receivePlaceComponentPacket(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        PlaceComponentC2SPacket packet = new PlaceComponentC2SPacket(buf);
        minecraftServer.executeSync(() -> {
            ServerWorld world = serverPlayerEntity.getWorld();
            if(world.getBlockState(packet.blockPos).isOf(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK) && world.getBlockEntity(packet.blockPos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                if(!serverPlayerEntity.getUuid().equals(integratedCircuitBlockEntity.getEditor())) return;
                integratedCircuitBlockEntity.getCircuit().placeComponentFromClient(packet.pos, packet.component, packet.rotation);
            }
        });
    }

    public static void receiveComponentInteraction(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        ComponentInteractionC2SPacket packet = new ComponentInteractionC2SPacket(buf);
        minecraftServer.executeSync(() -> {
            ServerWorld world = serverPlayerEntity.getWorld();
            if(world.getBlockState(packet.blockPos).isOf(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK) && world.getBlockEntity(packet.blockPos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                if(!serverPlayerEntity.getUuid().equals(integratedCircuitBlockEntity.getEditor())) return;
                integratedCircuitBlockEntity.getCircuit().useComponent(packet.pos, packet.blockPos);
            }
        });
    }
}
