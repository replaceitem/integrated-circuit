package net.replaceitem.integratedcircuit.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;

public class ServerPacketHandler {
    public static void receiveFinishEditingPacket(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        FinishEditingC2SPacket packet = new FinishEditingC2SPacket(buf);
        minecraftServer.executeSync(() -> {
            serverPlayerEntity.updateLastActionTime();
            ServerWorld serverWorld = serverPlayerEntity.getServerWorld();
            BlockEntity blockEntity = serverWorld.getBlockEntity(packet.pos);
            if (!(blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)) return;
            integratedCircuitBlockEntity.removeEditor(serverPlayerEntity);
        });
    }

    public static void receivePlaceComponentPacket(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        PlaceComponentC2SPacket packet = new PlaceComponentC2SPacket(buf);
        minecraftServer.executeSync(() -> {
            ServerWorld world = serverPlayerEntity.getServerWorld();
            if(world.getBlockState(packet.blockPos).isIn(IntegratedCircuit.INTEGRATED_CIRCUITS_BLOCK_TAG) && world.getBlockEntity(packet.blockPos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                if(!integratedCircuitBlockEntity.isEditing(serverPlayerEntity)) return;
                integratedCircuitBlockEntity.getCircuit().placeComponentState(packet.pos, packet.component, packet.rotation);
            }
        });
    }

    public static void receiveComponentInteraction(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        ComponentInteractionC2SPacket packet = new ComponentInteractionC2SPacket(buf);
        minecraftServer.executeSync(() -> {
            ServerWorld world = serverPlayerEntity.getServerWorld();
            if(world.getBlockState(packet.blockPos).isIn(IntegratedCircuit.INTEGRATED_CIRCUITS_BLOCK_TAG) && world.getBlockEntity(packet.blockPos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                if(!integratedCircuitBlockEntity.isEditing(serverPlayerEntity)) return;
                integratedCircuitBlockEntity.getCircuit().useComponent(packet.pos, serverPlayerEntity);
            }
        });
    }
}
