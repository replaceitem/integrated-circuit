package net.replaceitem.integratedcircuit.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;

public class ClientPacketHandler {
    public static void receiveEditIntegratedCircuitPacket(EditIntegratedCircuitS2CPacket packet, ClientPlayNetworking.Context context) {
        context.client().setScreen(new IntegratedCircuitScreen(packet.getClientCircuit(context.player().clientWorld, packet.pos()), packet.name()));
    }

    public static void receiveComponentUpdatePacket(ComponentUpdateS2CPacket packet, ClientPlayNetworking.Context context) {
        if(context.client().currentScreen instanceof IntegratedCircuitScreen integratedCircuitScreen) {
            integratedCircuitScreen.getClientCircuit().onComponentUpdateFromServer(packet.state(), packet.pos());
        }
    }
}
