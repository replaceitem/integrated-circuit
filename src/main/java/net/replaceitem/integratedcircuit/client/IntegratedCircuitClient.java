package net.replaceitem.integratedcircuit.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntityRenderer;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;
import net.replaceitem.integratedcircuit.network.ClientPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class IntegratedCircuitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), IntegratedCircuit.Blocks.CIRCUITS);

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if(view == null || pos == null || !(state.getBlock() instanceof IntegratedCircuitBlock block) || tintIndex > 3)
                return RedstoneWireBlock.getWireColor(0);

            FlatDirection circuitDirection = FlatDirection.VALUES[tintIndex];

            return RedstoneWireBlock.getWireColor(
                block.getPortRenderStrength(view, pos, circuitDirection)
            );
        }, IntegratedCircuit.Blocks.CIRCUITS);
        
        BlockEntityRendererFactories.register(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, IntegratedCircuitBlockEntityRenderer::new);

        DefaultConfig.initialize();

        ClientPlayNetworking.registerGlobalReceiver(EditIntegratedCircuitS2CPacket.ID, ClientPacketHandler::receiveEditIntegratedCircuitPacket);
        ClientPlayNetworking.registerGlobalReceiver(ComponentUpdateS2CPacket.ID, ClientPacketHandler::receiveComponentUpdatePacket);
    }
}
