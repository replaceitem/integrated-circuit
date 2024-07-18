package net.replaceitem.integratedcircuit;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.network.ServerPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.*;
import org.slf4j.Logger;

public class IntegratedCircuit implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "integrated_circuit"; // TODO - maybe make this dynamic

    public static final RegistryKey<Registry<Component>> COMPONENTS_REGISTRY_KEY = RegistryKey.ofRegistry(IntegratedCircuit.id("components"));
    public static final DefaultedRegistry<Component> COMPONENTS_REGISTRY = FabricRegistryBuilder
            .from(new SimpleDefaultedRegistry<>(IntegratedCircuit.id("air").toString(), COMPONENTS_REGISTRY_KEY, Lifecycle.stable(), true))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    
    @SuppressWarnings("deprecation")
    public static final DataComponentType<NbtComponent> CIRCUIT_DATA = DataComponentType.<NbtComponent>builder()
            .codec(NbtComponent.CODEC)
            .packetCodec(NbtComponent.PACKET_CODEC)
            .build();
    
    public static class Tags {
        public static final TagKey<Block> INTEGRATED_CIRCUITS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, id("integrated_circuits"));
        public static final TagKey<Item> INTEGRATED_CIRCUITS_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("integrated_circuits"));
        public static final TagKey<Item> DYEABLE_INTEGRATED_CIRCUITS_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("dyeable_integrated_circuits"));
    }

    public static class Blocks {
        public static final IntegratedCircuitBlock INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY));
        public static final IntegratedCircuitBlock WHITE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.WHITE));
        public static final IntegratedCircuitBlock ORANGE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.ORANGE));
        public static final IntegratedCircuitBlock MAGENTA_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.MAGENTA));
        public static final IntegratedCircuitBlock LIGHT_BLUE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.LIGHT_BLUE));
        public static final IntegratedCircuitBlock YELLOW_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.YELLOW));
        public static final IntegratedCircuitBlock LIME_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.LIME));
        public static final IntegratedCircuitBlock PINK_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.PINK));
        public static final IntegratedCircuitBlock GRAY_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.GRAY));
        public static final IntegratedCircuitBlock LIGHT_GRAY_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.LIGHT_GRAY));
        public static final IntegratedCircuitBlock CYAN_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.CYAN));
        public static final IntegratedCircuitBlock PURPLE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.PURPLE));
        public static final IntegratedCircuitBlock BLUE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.BLUE));
        public static final IntegratedCircuitBlock BROWN_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.BROWN));
        public static final IntegratedCircuitBlock GREEN_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.GREEN));
        public static final IntegratedCircuitBlock RED_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.RED));
        public static final IntegratedCircuitBlock BLACK_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.BLACK));

        public static final IntegratedCircuitBlock[] CIRCUITS = {
                INTEGRATED_CIRCUIT, WHITE_INTEGRATED_CIRCUIT, LIGHT_GRAY_INTEGRATED_CIRCUIT,
                GRAY_INTEGRATED_CIRCUIT, BLACK_INTEGRATED_CIRCUIT, BROWN_INTEGRATED_CIRCUIT,
                RED_INTEGRATED_CIRCUIT, ORANGE_INTEGRATED_CIRCUIT, YELLOW_INTEGRATED_CIRCUIT,
                LIME_INTEGRATED_CIRCUIT, GREEN_INTEGRATED_CIRCUIT, CYAN_INTEGRATED_CIRCUIT,
                LIGHT_BLUE_INTEGRATED_CIRCUIT, BLUE_INTEGRATED_CIRCUIT, PURPLE_INTEGRATED_CIRCUIT,
                MAGENTA_INTEGRATED_CIRCUIT, PINK_INTEGRATED_CIRCUIT
        };
    }
    
    public static class Items {
        public static final IntegratedCircuitItem INTEGRATED_CIRCUIT            = new IntegratedCircuitItem(Blocks.INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem WHITE_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.WHITE_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem ORANGE_INTEGRATED_CIRCUIT     = new IntegratedCircuitItem(Blocks.ORANGE_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem MAGENTA_INTEGRATED_CIRCUIT    = new IntegratedCircuitItem(Blocks.MAGENTA_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem LIGHT_BLUE_INTEGRATED_CIRCUIT = new IntegratedCircuitItem(Blocks.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem YELLOW_INTEGRATED_CIRCUIT     = new IntegratedCircuitItem(Blocks.YELLOW_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem LIME_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.LIME_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem PINK_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.PINK_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem GRAY_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.GRAY_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem LIGHT_GRAY_INTEGRATED_CIRCUIT = new IntegratedCircuitItem(Blocks.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem CYAN_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.CYAN_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem PURPLE_INTEGRATED_CIRCUIT     = new IntegratedCircuitItem(Blocks.PURPLE_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem BLUE_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.BLUE_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem BROWN_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.BROWN_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem GREEN_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.GREEN_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem RED_INTEGRATED_CIRCUIT        = new IntegratedCircuitItem(Blocks.RED_INTEGRATED_CIRCUIT);
        public static final IntegratedCircuitItem BLACK_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.BLACK_INTEGRATED_CIRCUIT);

        public static final IntegratedCircuitItem[] CIRCUITS = {
                INTEGRATED_CIRCUIT, WHITE_INTEGRATED_CIRCUIT, LIGHT_GRAY_INTEGRATED_CIRCUIT,
                GRAY_INTEGRATED_CIRCUIT, BLACK_INTEGRATED_CIRCUIT, BROWN_INTEGRATED_CIRCUIT,
                RED_INTEGRATED_CIRCUIT, ORANGE_INTEGRATED_CIRCUIT, YELLOW_INTEGRATED_CIRCUIT,
                LIME_INTEGRATED_CIRCUIT, GREEN_INTEGRATED_CIRCUIT, CYAN_INTEGRATED_CIRCUIT,
                LIGHT_BLUE_INTEGRATED_CIRCUIT, BLUE_INTEGRATED_CIRCUIT, PURPLE_INTEGRATED_CIRCUIT,
                MAGENTA_INTEGRATED_CIRCUIT, PINK_INTEGRATED_CIRCUIT
        };
    }

    public static final BlockEntityType<IntegratedCircuitBlockEntity> INTEGRATED_CIRCUIT_BLOCK_ENTITY = 
        BlockEntityType.Builder.create(IntegratedCircuitBlockEntity::new, Blocks.CIRCUITS).build();

    public static final SpecialRecipeSerializer<IntegratedCircuitCloningRecipe> CIRCUIT_CLONING_RECIPE = new SpecialRecipeSerializer<>(IntegratedCircuitCloningRecipe::new);
    public static final SpecialRecipeSerializer<IntegratedCircuitDyeingRecipe> CIRCUIT_DYEING_RECIPE = new SpecialRecipeSerializer<>(IntegratedCircuitDyeingRecipe::new);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        Components.register();
        
        Registry.register(Registries.DATA_COMPONENT_TYPE, id("circuit"), CIRCUIT_DATA);
        
        Registry.register(Registries.BLOCK, id("integrated_circuit"), Blocks.INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("white_integrated_circuit"), Blocks.WHITE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("orange_integrated_circuit"), Blocks.ORANGE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("magenta_integrated_circuit"), Blocks.MAGENTA_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("light_blue_integrated_circuit"), Blocks.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("yellow_integrated_circuit"), Blocks.YELLOW_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("lime_integrated_circuit"), Blocks.LIME_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("pink_integrated_circuit"), Blocks.PINK_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("gray_integrated_circuit"), Blocks.GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("light_gray_integrated_circuit"), Blocks.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("cyan_integrated_circuit"), Blocks.CYAN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("purple_integrated_circuit"), Blocks.PURPLE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("blue_integrated_circuit"), Blocks.BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("brown_integrated_circuit"), Blocks.BROWN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("green_integrated_circuit"), Blocks.GREEN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("red_integrated_circuit"), Blocks.RED_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, id("black_integrated_circuit"), Blocks.BLACK_INTEGRATED_CIRCUIT);

        Registry.register(Registries.ITEM, id("integrated_circuit"), Items.INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("white_integrated_circuit"), Items.WHITE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("orange_integrated_circuit"), Items.ORANGE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("magenta_integrated_circuit"), Items.MAGENTA_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("light_blue_integrated_circuit"), Items.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("yellow_integrated_circuit"), Items.YELLOW_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("lime_integrated_circuit"), Items.LIME_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("pink_integrated_circuit"), Items.PINK_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("gray_integrated_circuit"), Items.GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("light_gray_integrated_circuit"), Items.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("cyan_integrated_circuit"), Items.CYAN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("purple_integrated_circuit"), Items.PURPLE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("blue_integrated_circuit"), Items.BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("brown_integrated_circuit"), Items.BROWN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("green_integrated_circuit"), Items.GREEN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("red_integrated_circuit"), Items.RED_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, id("black_integrated_circuit"), Items.BLACK_INTEGRATED_CIRCUIT);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            for(Item item : Items.CIRCUITS) {
                entries.add(item);
            }
        });

        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("integrated_circuit"), INTEGRATED_CIRCUIT_BLOCK_ENTITY);

        RecipeSerializer.register("integrated_circuit:crafting_special_circuit_cloning", CIRCUIT_CLONING_RECIPE);
        RecipeSerializer.register("integrated_circuit:crafting_special_circuit_dyeing", CIRCUIT_DYEING_RECIPE);

        PayloadTypeRegistry.playC2S().register(ComponentInteractionC2SPacket.ID, ComponentInteractionC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(FinishEditingC2SPacket.ID, FinishEditingC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceComponentC2SPacket.ID, PlaceComponentC2SPacket.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(ComponentUpdateS2CPacket.ID, ComponentUpdateS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(EditIntegratedCircuitS2CPacket.ID, EditIntegratedCircuitS2CPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ComponentInteractionC2SPacket.ID, ServerPacketHandler::receiveComponentInteraction);
        ServerPlayNetworking.registerGlobalReceiver(FinishEditingC2SPacket.ID, ServerPacketHandler::receiveFinishEditingPacket);
        ServerPlayNetworking.registerGlobalReceiver(PlaceComponentC2SPacket.ID, ServerPacketHandler::receivePlaceComponentPacket);
    }
}
