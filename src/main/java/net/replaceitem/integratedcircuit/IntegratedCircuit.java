package net.replaceitem.integratedcircuit;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.network.ServerPacketHandler;
import net.replaceitem.integratedcircuit.network.packet.ComponentInteractionC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.network.packet.PlaceComponentC2SPacket;
import org.slf4j.Logger;

public class IntegratedCircuit implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "integrated_circuit"; // TODO - maybe make this dynamic
    public static final int DATA_VERSION = 1;

    public static final RegistryKey<Registry<Component>> COMPONENTS_REGISTRY_KEY = RegistryKey.ofRegistry(IntegratedCircuit.id("components"));
    public static final DefaultedRegistry<Component> COMPONENTS_REGISTRY = FabricRegistryBuilder
            .from(new SimpleDefaultedRegistry<>(IntegratedCircuit.id("air").toString(), COMPONENTS_REGISTRY_KEY, Lifecycle.stable(), true))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final TagKey<Block> INTEGRATED_CIRCUITS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, id("integrated_circuits"));
    public static final TagKey<Item> INTEGRATED_CIRCUITS_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("integrated_circuits"));
    public static final TagKey<Item> DYEABLE_INTEGRATED_CIRCUITS_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("dyeable_integrated_circuits"));

    public static final IntegratedCircuitBlock INTEGRATED_CIRCUIT_BLOCK            = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY));
    public static final IntegratedCircuitBlock WHITE_INTEGRATED_CIRCUIT_BLOCK      = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.WHITE));
    public static final IntegratedCircuitBlock ORANGE_INTEGRATED_CIRCUIT_BLOCK     = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.ORANGE));
    public static final IntegratedCircuitBlock MAGENTA_INTEGRATED_CIRCUIT_BLOCK    = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.MAGENTA));
    public static final IntegratedCircuitBlock LIGHT_BLUE_INTEGRATED_CIRCUIT_BLOCK = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.LIGHT_BLUE));
    public static final IntegratedCircuitBlock YELLOW_INTEGRATED_CIRCUIT_BLOCK     = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.YELLOW));
    public static final IntegratedCircuitBlock LIME_INTEGRATED_CIRCUIT_BLOCK       = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.LIME));
    public static final IntegratedCircuitBlock PINK_INTEGRATED_CIRCUIT_BLOCK       = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.PINK));
    public static final IntegratedCircuitBlock GRAY_INTEGRATED_CIRCUIT_BLOCK       = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.GRAY));
    public static final IntegratedCircuitBlock LIGHT_GRAY_INTEGRATED_CIRCUIT_BLOCK = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.LIGHT_GRAY));
    public static final IntegratedCircuitBlock CYAN_INTEGRATED_CIRCUIT_BLOCK       = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.CYAN));
    public static final IntegratedCircuitBlock PURPLE_INTEGRATED_CIRCUIT_BLOCK     = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.PURPLE));
    public static final IntegratedCircuitBlock BLUE_INTEGRATED_CIRCUIT_BLOCK       = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.BLUE));
    public static final IntegratedCircuitBlock BROWN_INTEGRATED_CIRCUIT_BLOCK      = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.BROWN));
    public static final IntegratedCircuitBlock GREEN_INTEGRATED_CIRCUIT_BLOCK      = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.GREEN));
    public static final IntegratedCircuitBlock RED_INTEGRATED_CIRCUIT_BLOCK        = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.RED));
    public static final IntegratedCircuitBlock BLACK_INTEGRATED_CIRCUIT_BLOCK      = new IntegratedCircuitBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY).mapColor(DyeColor.BLACK));

    public static final IntegratedCircuitBlock[] INTEGRATED_CIRCUIT_BLOCKS = {
            INTEGRATED_CIRCUIT_BLOCK, WHITE_INTEGRATED_CIRCUIT_BLOCK, LIGHT_GRAY_INTEGRATED_CIRCUIT_BLOCK,
            GRAY_INTEGRATED_CIRCUIT_BLOCK, BLACK_INTEGRATED_CIRCUIT_BLOCK, BROWN_INTEGRATED_CIRCUIT_BLOCK,
            RED_INTEGRATED_CIRCUIT_BLOCK, ORANGE_INTEGRATED_CIRCUIT_BLOCK, YELLOW_INTEGRATED_CIRCUIT_BLOCK,
            LIME_INTEGRATED_CIRCUIT_BLOCK, GREEN_INTEGRATED_CIRCUIT_BLOCK, CYAN_INTEGRATED_CIRCUIT_BLOCK,
            LIGHT_BLUE_INTEGRATED_CIRCUIT_BLOCK, BLUE_INTEGRATED_CIRCUIT_BLOCK, PURPLE_INTEGRATED_CIRCUIT_BLOCK,
            MAGENTA_INTEGRATED_CIRCUIT_BLOCK, PINK_INTEGRATED_CIRCUIT_BLOCK
    };

    public static final IntegratedCircuitItem INTEGRATED_CIRCUIT_ITEM            = new IntegratedCircuitItem(INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem WHITE_INTEGRATED_CIRCUIT_ITEM      = new IntegratedCircuitItem(WHITE_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem ORANGE_INTEGRATED_CIRCUIT_ITEM     = new IntegratedCircuitItem(ORANGE_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem MAGENTA_INTEGRATED_CIRCUIT_ITEM    = new IntegratedCircuitItem(MAGENTA_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem LIGHT_BLUE_INTEGRATED_CIRCUIT_ITEM = new IntegratedCircuitItem(LIGHT_BLUE_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem YELLOW_INTEGRATED_CIRCUIT_ITEM     = new IntegratedCircuitItem(YELLOW_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem LIME_INTEGRATED_CIRCUIT_ITEM       = new IntegratedCircuitItem(LIME_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem PINK_INTEGRATED_CIRCUIT_ITEM       = new IntegratedCircuitItem(PINK_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem GRAY_INTEGRATED_CIRCUIT_ITEM       = new IntegratedCircuitItem(GRAY_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem LIGHT_GRAY_INTEGRATED_CIRCUIT_ITEM = new IntegratedCircuitItem(LIGHT_GRAY_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem CYAN_INTEGRATED_CIRCUIT_ITEM       = new IntegratedCircuitItem(CYAN_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem PURPLE_INTEGRATED_CIRCUIT_ITEM     = new IntegratedCircuitItem(PURPLE_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem BLUE_INTEGRATED_CIRCUIT_ITEM       = new IntegratedCircuitItem(BLUE_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem BROWN_INTEGRATED_CIRCUIT_ITEM      = new IntegratedCircuitItem(BROWN_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem GREEN_INTEGRATED_CIRCUIT_ITEM      = new IntegratedCircuitItem(GREEN_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem RED_INTEGRATED_CIRCUIT_ITEM        = new IntegratedCircuitItem(RED_INTEGRATED_CIRCUIT_BLOCK);
    public static final IntegratedCircuitItem BLACK_INTEGRATED_CIRCUIT_ITEM      = new IntegratedCircuitItem(BLACK_INTEGRATED_CIRCUIT_BLOCK);

    public static final IntegratedCircuitItem[] INTEGRATED_CIRCUIT_ITEMS = {
            INTEGRATED_CIRCUIT_ITEM, WHITE_INTEGRATED_CIRCUIT_ITEM, LIGHT_GRAY_INTEGRATED_CIRCUIT_ITEM,
            GRAY_INTEGRATED_CIRCUIT_ITEM, BLACK_INTEGRATED_CIRCUIT_ITEM, BROWN_INTEGRATED_CIRCUIT_ITEM,
            RED_INTEGRATED_CIRCUIT_ITEM, ORANGE_INTEGRATED_CIRCUIT_ITEM, YELLOW_INTEGRATED_CIRCUIT_ITEM,
            LIME_INTEGRATED_CIRCUIT_ITEM, GREEN_INTEGRATED_CIRCUIT_ITEM, CYAN_INTEGRATED_CIRCUIT_ITEM,
            LIGHT_BLUE_INTEGRATED_CIRCUIT_ITEM, BLUE_INTEGRATED_CIRCUIT_ITEM, PURPLE_INTEGRATED_CIRCUIT_ITEM,
            MAGENTA_INTEGRATED_CIRCUIT_ITEM, PINK_INTEGRATED_CIRCUIT_ITEM
    };

    public static final BlockEntityType<IntegratedCircuitBlockEntity> INTEGRATED_CIRCUIT_BLOCK_ENTITY = 
        FabricBlockEntityTypeBuilder.create(IntegratedCircuitBlockEntity::new, INTEGRATED_CIRCUIT_BLOCKS).build();

    public static final SpecialRecipeSerializer<IntegratedCircuitCloningRecipe> CIRCUIT_CLONING_RECIPE = new SpecialRecipeSerializer<>(IntegratedCircuitCloningRecipe::new);
    public static final SpecialRecipeSerializer<IntegratedCircuitDyeingRecipe> CIRCUIT_DYEING_RECIPE = new SpecialRecipeSerializer<>(IntegratedCircuitDyeingRecipe::new);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        Components.register();
        
        Registry.register(Registries.BLOCK, id("integrated_circuit"), INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("white_integrated_circuit"), WHITE_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("orange_integrated_circuit"), ORANGE_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("magenta_integrated_circuit"), MAGENTA_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("light_blue_integrated_circuit"), LIGHT_BLUE_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("yellow_integrated_circuit"), YELLOW_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("lime_integrated_circuit"), LIME_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("pink_integrated_circuit"), PINK_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("gray_integrated_circuit"), GRAY_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("light_gray_integrated_circuit"), LIGHT_GRAY_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("cyan_integrated_circuit"), CYAN_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("purple_integrated_circuit"), PURPLE_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("blue_integrated_circuit"), BLUE_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("brown_integrated_circuit"), BROWN_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("green_integrated_circuit"), GREEN_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("red_integrated_circuit"), RED_INTEGRATED_CIRCUIT_BLOCK);
        Registry.register(Registries.BLOCK, id("black_integrated_circuit"), BLACK_INTEGRATED_CIRCUIT_BLOCK);

        Registry.register(Registries.ITEM, id("integrated_circuit"), INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("white_integrated_circuit"), WHITE_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("orange_integrated_circuit"), ORANGE_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("magenta_integrated_circuit"), MAGENTA_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("light_blue_integrated_circuit"), LIGHT_BLUE_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("yellow_integrated_circuit"), YELLOW_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("lime_integrated_circuit"), LIME_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("pink_integrated_circuit"), PINK_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("gray_integrated_circuit"), GRAY_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("light_gray_integrated_circuit"), LIGHT_GRAY_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("cyan_integrated_circuit"), CYAN_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("purple_integrated_circuit"), PURPLE_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("blue_integrated_circuit"), BLUE_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("brown_integrated_circuit"), BROWN_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("green_integrated_circuit"), GREEN_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("red_integrated_circuit"), RED_INTEGRATED_CIRCUIT_ITEM);
        Registry.register(Registries.ITEM, id("black_integrated_circuit"), BLACK_INTEGRATED_CIRCUIT_ITEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            for(Item item : INTEGRATED_CIRCUIT_ITEMS) {
                entries.add(item);
            }
        });

        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("integrated_circuit_block_entity"), INTEGRATED_CIRCUIT_BLOCK_ENTITY);

        RecipeSerializer.register("integrated_circuit:crafting_special_circuit_cloning", CIRCUIT_CLONING_RECIPE);
        RecipeSerializer.register("integrated_circuit:crafting_special_circuit_dyeing", CIRCUIT_DYEING_RECIPE);

        ServerPlayNetworking.registerGlobalReceiver(PlaceComponentC2SPacket.ID, ServerPacketHandler::receivePlaceComponentPacket);
        ServerPlayNetworking.registerGlobalReceiver(FinishEditingC2SPacket.ID, ServerPacketHandler::receiveFinishEditingPacket);
        ServerPlayNetworking.registerGlobalReceiver(ComponentInteractionC2SPacket.ID, ServerPacketHandler::receiveComponentInteraction);
    }


    public static NbtCompound putDataVersion(NbtCompound nbt) {
        return putDataVersion(nbt, IntegratedCircuit.DATA_VERSION);
    }

    public static NbtCompound putDataVersion(NbtCompound nbt, int dataVersion) {
        nbt.putInt("CircuitDataVersion", dataVersion);
        return nbt;
    }

    public static int getDataVersion(NbtCompound nbt, int fallback) {
        return nbt.contains("CircuitDataVersion", NbtElement.NUMBER_TYPE) ? nbt.getInt("CircuitDataVersion") : fallback;
    }
}
