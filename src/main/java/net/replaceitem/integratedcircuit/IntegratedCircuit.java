package net.replaceitem.integratedcircuit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.registry.Registry;
import net.replaceitem.integratedcircuit.network.ServerPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class IntegratedCircuit implements ModInitializer {

    public final static String MOD_ID = "integrated_circuit"; // TODO - maybe make this dynamic
    public final static Block INTEGRATED_CIRCUIT_BLOCK = new IntegratedCircuitBlock();
    public static final Item INTEGRATED_CIRCUIT_ITEM = new IntegratedCircuitItem();
    public static final BlockEntityType<IntegratedCircuitBlockEntity> INTEGRATED_CIRCUIT_BLOCK_ENTITY = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new IntegratedCircuitIdentifier("integrated_circuit_block_entity"),
            FabricBlockEntityTypeBuilder.create(IntegratedCircuitBlockEntity::new, INTEGRATED_CIRCUIT_BLOCK).build()
    );

    public static final SpecialRecipeSerializer<IntegratedCircuitCloningRecipe> CIRCUIT_CLONING = RecipeSerializer.register("integrated_circuit:crafting_special_circuit_cloning", new SpecialRecipeSerializer<IntegratedCircuitCloningRecipe>(IntegratedCircuitCloningRecipe::new));


    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new IntegratedCircuitIdentifier("integrated_circuit"), INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registry.ITEM, new IntegratedCircuitIdentifier("integrated_circuit"), INTEGRATED_CIRCUIT_ITEM);
        ServerPlayNetworking.registerGlobalReceiver(PlaceComponentC2SPacket.ID, ServerPacketHandler::receivePlaceComponentPacket);
        ServerPlayNetworking.registerGlobalReceiver(FinishEditingC2SPacket.ID, ServerPacketHandler::receiveFinishEditingPacket);
        ServerPlayNetworking.registerGlobalReceiver(ComponentInteractionC2SPacket.ID, ServerPacketHandler::receiveComponentInteraction);
    }
}
