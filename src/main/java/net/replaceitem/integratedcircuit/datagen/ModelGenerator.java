package net.replaceitem.integratedcircuit.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

import java.util.Optional;

import static net.minecraft.client.data.TextureMap.getId;
import static net.minecraft.client.data.TextureMap.getSubId;
import static net.replaceitem.integratedcircuit.IntegratedCircuit.Blocks.*;

public class ModelGenerator extends FabricModelProvider {

    public static final TextureKey SIDES = TextureKey.of("sides");
    public static final Identifier TEMPLATE_MODEL_ID = Identifier.of(IntegratedCircuit.MOD_ID, "block/template_integrated_circuit");
    public static final Model TEMPLATE_CIRCUIT = new Model(Optional.of(TEMPLATE_MODEL_ID), Optional.empty(), TextureKey.TOP, SIDES, TextureKey.BOTTOM, TextureKey.PARTICLE);
    
    public static TextureMap circuitTextures(Block block, Block bottomBlock) {
        return new TextureMap()
                .put(TextureKey.TOP, getSubId(block, "_top"))
                .put(SIDES, getSubId(block, "_sides"))
                .put(TextureKey.BOTTOM, getId(bottomBlock))
                .put(TextureKey.PARTICLE, getSubId(block, "_top"));
    }

    public ModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        registerCircuit(blockStateModelGenerator, INTEGRATED_CIRCUIT, Blocks.SMOOTH_STONE);
        registerCircuit(blockStateModelGenerator, WHITE_INTEGRATED_CIRCUIT, Blocks.WHITE_CONCRETE);
        registerCircuit(blockStateModelGenerator, ORANGE_INTEGRATED_CIRCUIT, Blocks.ORANGE_CONCRETE);
        registerCircuit(blockStateModelGenerator, MAGENTA_INTEGRATED_CIRCUIT, Blocks.MAGENTA_CONCRETE);
        registerCircuit(blockStateModelGenerator, LIGHT_BLUE_INTEGRATED_CIRCUIT, Blocks.LIGHT_BLUE_CONCRETE);
        registerCircuit(blockStateModelGenerator, YELLOW_INTEGRATED_CIRCUIT, Blocks.YELLOW_CONCRETE);
        registerCircuit(blockStateModelGenerator, LIME_INTEGRATED_CIRCUIT, Blocks.LIME_CONCRETE);
        registerCircuit(blockStateModelGenerator, PINK_INTEGRATED_CIRCUIT, Blocks.PINK_CONCRETE);
        registerCircuit(blockStateModelGenerator, GRAY_INTEGRATED_CIRCUIT, Blocks.GRAY_CONCRETE);
        registerCircuit(blockStateModelGenerator, LIGHT_GRAY_INTEGRATED_CIRCUIT, Blocks.LIGHT_GRAY_CONCRETE);
        registerCircuit(blockStateModelGenerator, CYAN_INTEGRATED_CIRCUIT, Blocks.CYAN_CONCRETE);
        registerCircuit(blockStateModelGenerator, PURPLE_INTEGRATED_CIRCUIT, Blocks.PURPLE_CONCRETE);
        registerCircuit(blockStateModelGenerator, BLUE_INTEGRATED_CIRCUIT, Blocks.BLUE_CONCRETE);
        registerCircuit(blockStateModelGenerator, BROWN_INTEGRATED_CIRCUIT, Blocks.BROWN_CONCRETE);
        registerCircuit(blockStateModelGenerator, GREEN_INTEGRATED_CIRCUIT, Blocks.GREEN_CONCRETE);
        registerCircuit(blockStateModelGenerator, RED_INTEGRATED_CIRCUIT, Blocks.RED_CONCRETE);
        registerCircuit(blockStateModelGenerator, BLACK_INTEGRATED_CIRCUIT, Blocks.BLACK_CONCRETE);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }

    private static final BlockStateVariantMap<ModelVariantOperator> NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS = BlockStateVariantMap.operations(
                    Properties.HORIZONTAL_FACING
            )
            .register(Direction.EAST, BlockStateModelGenerator.ROTATE_Y_90)
            .register(Direction.SOUTH, BlockStateModelGenerator.ROTATE_Y_180)
            .register(Direction.WEST, BlockStateModelGenerator.ROTATE_Y_270)
            .register(Direction.NORTH, BlockStateModelGenerator.NO_OP);
    
    public final void registerCircuit(BlockStateModelGenerator blockStateModelGenerator, Block block, Block baseBlock) {
        TextureMap textureMap = circuitTextures(block, baseBlock);
        
        Identifier modelId = TEMPLATE_CIRCUIT.upload(block, textureMap, blockStateModelGenerator.modelCollector);
        blockStateModelGenerator.blockStateCollector
                .accept(
                        VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.createWeightedVariant(modelId))
                                .coordinate(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS)
                );

        TintSource wireOffTint = ItemModels.constantTintSource(RedstoneWireBlock.getWireColor(0));
        blockStateModelGenerator.itemModelOutput.accept(block.asItem(), ItemModels.tinted(modelId, wireOffTint, wireOffTint, wireOffTint, wireOffTint));
    }
}
