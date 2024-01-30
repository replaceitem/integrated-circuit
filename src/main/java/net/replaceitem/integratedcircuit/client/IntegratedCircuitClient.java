package net.replaceitem.integratedcircuit.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.RedstoneView;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;
import net.replaceitem.integratedcircuit.network.ClientPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;
import net.replaceitem.integratedcircuit.util.FlatDirection;

@Environment(EnvType.CLIENT)
public class IntegratedCircuitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCKS);

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if(!(state.getBlock() instanceof IntegratedCircuitBlock) || tintIndex > 3) {
                return RedstoneWireBlock.getWireColor(0);
            }
            IntegratedCircuitBlock block = ((IntegratedCircuitBlock)state.getBlock());
            FlatDirection circuitDirection = FlatDirection.VALUES[tintIndex];

            return RedstoneWireBlock.getWireColor(Math.max(
                block.getInputPower((RedstoneView)view, pos, circuitDirection),
                block.getOutputPower(view, pos, circuitDirection)
            ));
        }, IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCKS);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return RedstoneWireBlock.getWireColor(0);
        }, IntegratedCircuit.INTEGRATED_CIRCUIT_ITEMS);

        DefaultConfig.initialize();

        ClientPlayNetworking.registerGlobalReceiver(EditIntegratedCircuitS2CPacket.ID, ClientPacketHandler::receiveEditIntegratedCircuitPacket);
        ClientPlayNetworking.registerGlobalReceiver(ComponentUpdateS2CPacket.ID, ClientPacketHandler::receiveComponentUpdatePacket);
    }
}
