package net.replaceitem.integratedcircuit;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.network.ServerPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.*;
import org.jetbrains.annotations.Nullable;
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
    public static final ComponentType<NbtComponent> CIRCUIT_DATA = ComponentType.<NbtComponent>builder()
            .codec(NbtComponent.CODEC)
            .packetCodec(NbtComponent.PACKET_CODEC)
            .build();
    
    public static class Tags {
        public static final TagKey<Block> INTEGRATED_CIRCUITS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, id("integrated_circuits"));
        public static final TagKey<Item> INTEGRATED_CIRCUITS_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("integrated_circuits"));
        public static final TagKey<Item> DYEABLE_INTEGRATED_CIRCUITS_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("dyeable_integrated_circuits"));
    }

    public static class Blocks {
        private static class Keys {
            public static final RegistryKey<Block> INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("integrated_circuit"));
            public static final RegistryKey<Block> WHITE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("white_integrated_circuit"));
            public static final RegistryKey<Block> ORANGE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("orange_integrated_circuit"));
            public static final RegistryKey<Block> MAGENTA_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("magenta_integrated_circuit"));
            public static final RegistryKey<Block> LIGHT_BLUE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("light_blue_integrated_circuit"));
            public static final RegistryKey<Block> YELLOW_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("yellow_integrated_circuit"));
            public static final RegistryKey<Block> LIME_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("lime_integrated_circuit"));
            public static final RegistryKey<Block> PINK_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("pink_integrated_circuit"));
            public static final RegistryKey<Block> GRAY_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("gray_integrated_circuit"));
            public static final RegistryKey<Block> LIGHT_GRAY_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("light_gray_integrated_circuit"));
            public static final RegistryKey<Block> CYAN_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("cyan_integrated_circuit"));
            public static final RegistryKey<Block> PURPLE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("purple_integrated_circuit"));
            public static final RegistryKey<Block> BLUE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("blue_integrated_circuit"));
            public static final RegistryKey<Block> BROWN_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("brown_integrated_circuit"));
            public static final RegistryKey<Block> GREEN_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("green_integrated_circuit"));
            public static final RegistryKey<Block> RED_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("red_integrated_circuit"));
            public static final RegistryKey<Block> BLACK_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.BLOCK, id("black_integrated_circuit"));
        }
        
        public static final IntegratedCircuitBlock INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.INTEGRATED_CIRCUIT, null));
        public static final IntegratedCircuitBlock WHITE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.WHITE_INTEGRATED_CIRCUIT, DyeColor.WHITE));
        public static final IntegratedCircuitBlock ORANGE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.ORANGE_INTEGRATED_CIRCUIT, DyeColor.ORANGE));
        public static final IntegratedCircuitBlock MAGENTA_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.MAGENTA_INTEGRATED_CIRCUIT, DyeColor.MAGENTA));
        public static final IntegratedCircuitBlock LIGHT_BLUE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.LIGHT_BLUE_INTEGRATED_CIRCUIT, DyeColor.LIGHT_BLUE));
        public static final IntegratedCircuitBlock YELLOW_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.YELLOW_INTEGRATED_CIRCUIT, DyeColor.YELLOW));
        public static final IntegratedCircuitBlock LIME_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.LIME_INTEGRATED_CIRCUIT, DyeColor.LIME));
        public static final IntegratedCircuitBlock PINK_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.PINK_INTEGRATED_CIRCUIT, DyeColor.PINK));
        public static final IntegratedCircuitBlock GRAY_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.GRAY_INTEGRATED_CIRCUIT, DyeColor.GRAY));
        public static final IntegratedCircuitBlock LIGHT_GRAY_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.LIGHT_GRAY_INTEGRATED_CIRCUIT, DyeColor.LIGHT_GRAY));
        public static final IntegratedCircuitBlock CYAN_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.CYAN_INTEGRATED_CIRCUIT, DyeColor.CYAN));
        public static final IntegratedCircuitBlock PURPLE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.PURPLE_INTEGRATED_CIRCUIT, DyeColor.PURPLE));
        public static final IntegratedCircuitBlock BLUE_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.BLUE_INTEGRATED_CIRCUIT, DyeColor.BLUE));
        public static final IntegratedCircuitBlock BROWN_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.BROWN_INTEGRATED_CIRCUIT, DyeColor.BROWN));
        public static final IntegratedCircuitBlock GREEN_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.GREEN_INTEGRATED_CIRCUIT, DyeColor.GREEN));
        public static final IntegratedCircuitBlock RED_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.RED_INTEGRATED_CIRCUIT, DyeColor.RED));
        public static final IntegratedCircuitBlock BLACK_INTEGRATED_CIRCUIT = new IntegratedCircuitBlock(circuitSettings(Keys.BLACK_INTEGRATED_CIRCUIT, DyeColor.BLACK));
        
        private static AbstractBlock.Settings circuitSettings(RegistryKey<Block> registryKey, @Nullable DyeColor color) {
            AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.WOOD)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .registryKey(registryKey);
            if(color != null) settings = settings.mapColor(color);
            return settings;
        }

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
        private static class Keys {
            public static final RegistryKey<Item> INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("integrated_circuit"));
            public static final RegistryKey<Item> WHITE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("white_integrated_circuit"));
            public static final RegistryKey<Item> ORANGE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("orange_integrated_circuit"));
            public static final RegistryKey<Item> MAGENTA_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("magenta_integrated_circuit"));
            public static final RegistryKey<Item> LIGHT_BLUE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("light_blue_integrated_circuit"));
            public static final RegistryKey<Item> YELLOW_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("yellow_integrated_circuit"));
            public static final RegistryKey<Item> LIME_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("lime_integrated_circuit"));
            public static final RegistryKey<Item> PINK_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("pink_integrated_circuit"));
            public static final RegistryKey<Item> GRAY_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("gray_integrated_circuit"));
            public static final RegistryKey<Item> LIGHT_GRAY_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("light_gray_integrated_circuit"));
            public static final RegistryKey<Item> CYAN_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("cyan_integrated_circuit"));
            public static final RegistryKey<Item> PURPLE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("purple_integrated_circuit"));
            public static final RegistryKey<Item> BLUE_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("blue_integrated_circuit"));
            public static final RegistryKey<Item> BROWN_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("brown_integrated_circuit"));
            public static final RegistryKey<Item> GREEN_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("green_integrated_circuit"));
            public static final RegistryKey<Item> RED_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("red_integrated_circuit"));
            public static final RegistryKey<Item> BLACK_INTEGRATED_CIRCUIT = RegistryKey.of(RegistryKeys.ITEM, id("black_integrated_circuit"));
        }
        
        public static final IntegratedCircuitItem INTEGRATED_CIRCUIT            = new IntegratedCircuitItem(Blocks.INTEGRATED_CIRCUIT, circuitSettings(Keys.INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem WHITE_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.WHITE_INTEGRATED_CIRCUIT, circuitSettings(Keys.WHITE_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem ORANGE_INTEGRATED_CIRCUIT     = new IntegratedCircuitItem(Blocks.ORANGE_INTEGRATED_CIRCUIT, circuitSettings(Keys.ORANGE_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem MAGENTA_INTEGRATED_CIRCUIT    = new IntegratedCircuitItem(Blocks.MAGENTA_INTEGRATED_CIRCUIT, circuitSettings(Keys.MAGENTA_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem LIGHT_BLUE_INTEGRATED_CIRCUIT = new IntegratedCircuitItem(Blocks.LIGHT_BLUE_INTEGRATED_CIRCUIT, circuitSettings(Keys.LIGHT_BLUE_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem YELLOW_INTEGRATED_CIRCUIT     = new IntegratedCircuitItem(Blocks.YELLOW_INTEGRATED_CIRCUIT, circuitSettings(Keys.YELLOW_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem LIME_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.LIME_INTEGRATED_CIRCUIT, circuitSettings(Keys.LIME_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem PINK_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.PINK_INTEGRATED_CIRCUIT, circuitSettings(Keys.PINK_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem GRAY_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.GRAY_INTEGRATED_CIRCUIT, circuitSettings(Keys.GRAY_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem LIGHT_GRAY_INTEGRATED_CIRCUIT = new IntegratedCircuitItem(Blocks.LIGHT_GRAY_INTEGRATED_CIRCUIT, circuitSettings(Keys.LIGHT_GRAY_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem CYAN_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.CYAN_INTEGRATED_CIRCUIT, circuitSettings(Keys.CYAN_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem PURPLE_INTEGRATED_CIRCUIT     = new IntegratedCircuitItem(Blocks.PURPLE_INTEGRATED_CIRCUIT, circuitSettings(Keys.PURPLE_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem BLUE_INTEGRATED_CIRCUIT       = new IntegratedCircuitItem(Blocks.BLUE_INTEGRATED_CIRCUIT, circuitSettings(Keys.BLUE_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem BROWN_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.BROWN_INTEGRATED_CIRCUIT, circuitSettings(Keys.BROWN_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem GREEN_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.GREEN_INTEGRATED_CIRCUIT, circuitSettings(Keys.GREEN_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem RED_INTEGRATED_CIRCUIT        = new IntegratedCircuitItem(Blocks.RED_INTEGRATED_CIRCUIT, circuitSettings(Keys.RED_INTEGRATED_CIRCUIT));
        public static final IntegratedCircuitItem BLACK_INTEGRATED_CIRCUIT      = new IntegratedCircuitItem(Blocks.BLACK_INTEGRATED_CIRCUIT, circuitSettings(Keys.BLACK_INTEGRATED_CIRCUIT));
        
        private static Item.Settings circuitSettings(RegistryKey<Item> registryKey) {
            return new Item.Settings()
                    .registryKey(registryKey)
                    .useBlockPrefixedTranslationKey();
        }

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
            FabricBlockEntityTypeBuilder.create(IntegratedCircuitBlockEntity::new, Blocks.CIRCUITS).build();

    public static final RecipeSerializer<? extends SpecialCraftingRecipe> CIRCUIT_CLONING_RECIPE = new SpecialCraftingRecipe.SpecialRecipeSerializer<>(IntegratedCircuitCloningRecipe::new);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        Components.register();
        
        Registry.register(Registries.DATA_COMPONENT_TYPE, id("circuit"), CIRCUIT_DATA);
        
        Registry.register(Registries.BLOCK, Blocks.Keys.INTEGRATED_CIRCUIT, Blocks.INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.WHITE_INTEGRATED_CIRCUIT, Blocks.WHITE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.ORANGE_INTEGRATED_CIRCUIT, Blocks.ORANGE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.MAGENTA_INTEGRATED_CIRCUIT, Blocks.MAGENTA_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.LIGHT_BLUE_INTEGRATED_CIRCUIT, Blocks.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.YELLOW_INTEGRATED_CIRCUIT, Blocks.YELLOW_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.LIME_INTEGRATED_CIRCUIT, Blocks.LIME_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.PINK_INTEGRATED_CIRCUIT, Blocks.PINK_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.GRAY_INTEGRATED_CIRCUIT, Blocks.GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.LIGHT_GRAY_INTEGRATED_CIRCUIT, Blocks.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.CYAN_INTEGRATED_CIRCUIT, Blocks.CYAN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.PURPLE_INTEGRATED_CIRCUIT, Blocks.PURPLE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.BLUE_INTEGRATED_CIRCUIT, Blocks.BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.BROWN_INTEGRATED_CIRCUIT, Blocks.BROWN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.GREEN_INTEGRATED_CIRCUIT, Blocks.GREEN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.RED_INTEGRATED_CIRCUIT, Blocks.RED_INTEGRATED_CIRCUIT);
        Registry.register(Registries.BLOCK, Blocks.Keys.BLACK_INTEGRATED_CIRCUIT, Blocks.BLACK_INTEGRATED_CIRCUIT);

        Registry.register(Registries.ITEM, Items.Keys.INTEGRATED_CIRCUIT, Items.INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.WHITE_INTEGRATED_CIRCUIT, Items.WHITE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.ORANGE_INTEGRATED_CIRCUIT, Items.ORANGE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.MAGENTA_INTEGRATED_CIRCUIT, Items.MAGENTA_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.LIGHT_BLUE_INTEGRATED_CIRCUIT, Items.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.YELLOW_INTEGRATED_CIRCUIT, Items.YELLOW_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.LIME_INTEGRATED_CIRCUIT, Items.LIME_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.PINK_INTEGRATED_CIRCUIT, Items.PINK_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.GRAY_INTEGRATED_CIRCUIT, Items.GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.LIGHT_GRAY_INTEGRATED_CIRCUIT, Items.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.CYAN_INTEGRATED_CIRCUIT, Items.CYAN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.PURPLE_INTEGRATED_CIRCUIT, Items.PURPLE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.BLUE_INTEGRATED_CIRCUIT, Items.BLUE_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.BROWN_INTEGRATED_CIRCUIT, Items.BROWN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.GREEN_INTEGRATED_CIRCUIT, Items.GREEN_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.RED_INTEGRATED_CIRCUIT, Items.RED_INTEGRATED_CIRCUIT);
        Registry.register(Registries.ITEM, Items.Keys.BLACK_INTEGRATED_CIRCUIT, Items.BLACK_INTEGRATED_CIRCUIT);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            for(Item item : Items.CIRCUITS) {
                entries.add(item);
            }
        });

        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("integrated_circuit"), INTEGRATED_CIRCUIT_BLOCK_ENTITY);

        Registry.register(Registries.RECIPE_SERIALIZER, id("crafting_special_circuit_cloning"), CIRCUIT_CLONING_RECIPE);

        PayloadTypeRegistry.playC2S().register(ComponentInteractionC2SPacket.ID, ComponentInteractionC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(FinishEditingC2SPacket.ID, FinishEditingC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceComponentC2SPacket.ID, PlaceComponentC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RenameCircuitC2SPacket.ID, RenameCircuitC2SPacket.PACKET_CODEC);

        PayloadTypeRegistry.playS2C().register(CircuitNameUpdateS2CPacket.ID, CircuitNameUpdateS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ComponentUpdateS2CPacket.ID, ComponentUpdateS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(EditIntegratedCircuitS2CPacket.ID, EditIntegratedCircuitS2CPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ComponentInteractionC2SPacket.ID, ServerPacketHandler::receiveComponentInteraction);
        ServerPlayNetworking.registerGlobalReceiver(FinishEditingC2SPacket.ID, ServerPacketHandler::receiveFinishEditingPacket);
        ServerPlayNetworking.registerGlobalReceiver(PlaceComponentC2SPacket.ID, ServerPacketHandler::receivePlaceComponentPacket);
        ServerPlayNetworking.registerGlobalReceiver(RenameCircuitC2SPacket.ID, ServerPacketHandler::receiveRenameCircuitPacket);
    }
}
