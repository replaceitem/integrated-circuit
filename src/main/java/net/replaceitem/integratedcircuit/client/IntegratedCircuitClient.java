package net.replaceitem.integratedcircuit.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;
import net.replaceitem.integratedcircuit.network.ClientPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.CircuitNameUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;

public class IntegratedCircuitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockColorRegistry.register(IntegratedCircuitBlock.createBlockTintSources(), IntegratedCircuit.Blocks.CIRCUITS);

        DefaultConfig.initialize();
        
        BlockEntityRenderers.register(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, IntegratedCircuitBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(CircuitNameUpdateS2CPacket.ID, ClientPacketHandler::receiveCircuitNameUpdatePacket);
        ClientPlayNetworking.registerGlobalReceiver(EditIntegratedCircuitS2CPacket.ID, ClientPacketHandler::receiveEditIntegratedCircuitPacket);
        ClientPlayNetworking.registerGlobalReceiver(ComponentUpdateS2CPacket.ID, ClientPacketHandler::receiveComponentUpdatePacket);
    }
}
