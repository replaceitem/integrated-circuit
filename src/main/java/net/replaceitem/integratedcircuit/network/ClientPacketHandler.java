package net.replaceitem.integratedcircuit.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;

@Environment(EnvType.CLIENT)
public class ClientPacketHandler {
    public static void receiveEditIntegratedCircuitPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        EditIntegratedCircuitS2CPacket packet = new EditIntegratedCircuitS2CPacket(buf);
        client.executeSync(() -> client.setScreen(new IntegratedCircuitScreen(packet.getClientCircuit(handler.getWorld()), packet.name, packet.pos)));
    }

    public static void receiveComponentUpdatePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ComponentUpdateS2CPacket packet = new ComponentUpdateS2CPacket(buf);
        client.executeSync(() -> {
            if(client.currentScreen instanceof IntegratedCircuitScreen integratedCircuitScreen) {
                integratedCircuitScreen.getClientCircuit().onComponentUpdateFromServer(packet.state, packet.pos);
            }
        });
    }
}
