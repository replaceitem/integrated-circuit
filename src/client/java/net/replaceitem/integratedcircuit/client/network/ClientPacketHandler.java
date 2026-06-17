package net.replaceitem.integratedcircuit.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.replaceitem.integratedcircuit.client.circuit.ClientCircuitSerializer;
import net.replaceitem.integratedcircuit.client.circuit.context.ClientWorldClientCircuitContext;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.network.packet.CircuitNameUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;

@SuppressWarnings("resource")
public class ClientPacketHandler {
    public static void receiveEditIntegratedCircuitPacket(EditIntegratedCircuitS2CPacket packet, ClientPlayNetworking.Context context) {
        ClientLevel level = context.client().level;
        if(level == null) return;

        var serializer = new ClientCircuitSerializer(packet.circuitNbt());
        var circuit = serializer.readClientCircuit(new ClientWorldClientCircuitContext(level, packet.pos()));

        context.client().setScreenAndShow(new IntegratedCircuitScreen(circuit, packet.customName()));
    }

    public static void receiveCircuitNameUpdatePacket(CircuitNameUpdateS2CPacket packet, ClientPlayNetworking.Context context) {
        if (context.client().gui.screen() instanceof IntegratedCircuitScreen integratedCircuitScreen) {
            integratedCircuitScreen.updateCustomNameForExternalChange(packet.newName());
        }
    }

    public static void receiveComponentUpdatePacket(ComponentUpdateS2CPacket packet, ClientPlayNetworking.Context context) {
        if (context.client().gui.screen() instanceof IntegratedCircuitScreen integratedCircuitScreen) {
            integratedCircuitScreen.getClientCircuit().onComponentUpdateFromServer(
                packet.state(),
                packet.pos()
            );
        }
    }
}
