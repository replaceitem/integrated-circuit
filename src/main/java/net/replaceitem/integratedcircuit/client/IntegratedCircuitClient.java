package net.replaceitem.integratedcircuit.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.replaceitem.integratedcircuit.network.ClientPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;

@Environment(EnvType.CLIENT)
public class IntegratedCircuitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(EditIntegratedCircuitS2CPacket.ID, ClientPacketHandler::receiveEditIntegratedCircuitPacket);
        ClientPlayNetworking.registerGlobalReceiver(ComponentUpdateS2CPacket.ID, ClientPacketHandler::receiveComponentUpdatePacket);
    }
}
