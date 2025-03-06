package net.replaceitem.integratedcircuit.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.network.packet.CircuitNameUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;

public class ClientPacketHandler {
    public static void receiveEditIntegratedCircuitPacket(EditIntegratedCircuitS2CPacket packet, ClientPlayNetworking.Context context) {
        context.client().setScreen(
            new IntegratedCircuitScreen(
                packet.getClientCircuit(
                    context.player().clientWorld,
                    packet.pos()
                ),
                packet.customName()
            )
        );
    }

    public static void receiveCircuitNameUpdatePacket(CircuitNameUpdateS2CPacket packet, ClientPlayNetworking.Context context) {
        ClientWorld clientWorld = context.player().clientWorld;
        BlockEntity blockEntity = clientWorld.getBlockEntity(packet.pos());

        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            integratedCircuitBlockEntity.setCustomName(packet.newName());
        }
    }

    public static void receiveComponentUpdatePacket(ComponentUpdateS2CPacket packet, ClientPlayNetworking.Context context) {
        if (context.client().currentScreen instanceof IntegratedCircuitScreen integratedCircuitScreen) {
            integratedCircuitScreen.getClientCircuit().onComponentUpdateFromServer(
                packet.state(),
                packet.pos()
            );
        }
    }
}
