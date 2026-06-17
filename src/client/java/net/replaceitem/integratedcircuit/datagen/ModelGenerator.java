package net.replaceitem.integratedcircuit.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

import java.util.Optional;

import static net.minecraft.client.data.models.model.TextureMapping.getBlockTexture;
import static net.replaceitem.integratedcircuit.IntegratedCircuit.Blocks.*;

public class ModelGenerator extends FabricModelProvider {

    public static final TextureSlot SIDES = TextureSlot.create("sides");
    public static final Identifier TEMPLATE_MODEL_ID = Identifier.fromNamespaceAndPath(IntegratedCircuit.MOD_ID, "block/template_integrated_circuit");
    public static final ModelTemplate TEMPLATE_CIRCUIT = new ModelTemplate(Optional.of(TEMPLATE_MODEL_ID), Optional.empty(), TextureSlot.TOP, SIDES, TextureSlot.BOTTOM, TextureSlot.PARTICLE);
    
    public static TextureMapping circuitTextures(Block block, Block bottomBlock) {
        return new TextureMapping()
                .put(TextureSlot.TOP, getBlockTexture(block, "_top"))
                .put(SIDES, getBlockTexture(block, "_sides"))
                .put(TextureSlot.BOTTOM, getBlockTexture(bottomBlock))
                .put(TextureSlot.PARTICLE, getBlockTexture(block, "_top"));
    }

    public ModelGenerator(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        registerCircuit(blockStateModelGenerator, INTEGRATED_CIRCUIT, Blocks.SMOOTH_STONE);
        registerCircuit(blockStateModelGenerator, WHITE_INTEGRATED_CIRCUIT, Blocks.CONCRETE.white());
        registerCircuit(blockStateModelGenerator, ORANGE_INTEGRATED_CIRCUIT, Blocks.CONCRETE.orange());
        registerCircuit(blockStateModelGenerator, MAGENTA_INTEGRATED_CIRCUIT, Blocks.CONCRETE.magenta());
        registerCircuit(blockStateModelGenerator, LIGHT_BLUE_INTEGRATED_CIRCUIT, Blocks.CONCRETE.lightBlue());
        registerCircuit(blockStateModelGenerator, YELLOW_INTEGRATED_CIRCUIT, Blocks.CONCRETE.yellow());
        registerCircuit(blockStateModelGenerator, LIME_INTEGRATED_CIRCUIT, Blocks.CONCRETE.lime());
        registerCircuit(blockStateModelGenerator, PINK_INTEGRATED_CIRCUIT, Blocks.CONCRETE.pink());
        registerCircuit(blockStateModelGenerator, GRAY_INTEGRATED_CIRCUIT, Blocks.CONCRETE.gray());
        registerCircuit(blockStateModelGenerator, LIGHT_GRAY_INTEGRATED_CIRCUIT, Blocks.CONCRETE.lightGray());
        registerCircuit(blockStateModelGenerator, CYAN_INTEGRATED_CIRCUIT, Blocks.CONCRETE.cyan());
        registerCircuit(blockStateModelGenerator, PURPLE_INTEGRATED_CIRCUIT, Blocks.CONCRETE.purple());
        registerCircuit(blockStateModelGenerator, BLUE_INTEGRATED_CIRCUIT, Blocks.CONCRETE.blue());
        registerCircuit(blockStateModelGenerator, BROWN_INTEGRATED_CIRCUIT, Blocks.CONCRETE.brown());
        registerCircuit(blockStateModelGenerator, GREEN_INTEGRATED_CIRCUIT, Blocks.CONCRETE.green());
        registerCircuit(blockStateModelGenerator, RED_INTEGRATED_CIRCUIT, Blocks.CONCRETE.red());
        registerCircuit(blockStateModelGenerator, BLACK_INTEGRATED_CIRCUIT, Blocks.CONCRETE.black());
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {

    }

    private static final PropertyDispatch<VariantMutator> NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS = PropertyDispatch.modify(
                    BlockStateProperties.HORIZONTAL_FACING
            )
            .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
            .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
            .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
            .select(Direction.NORTH, BlockModelGenerators.NOP);
    
    public final void registerCircuit(BlockModelGenerators blockStateModelGenerator, Block block, Block baseBlock) {
        TextureMapping textureMap = circuitTextures(block, baseBlock);
        
        Identifier modelId = TEMPLATE_CIRCUIT.create(block, textureMap, blockStateModelGenerator.modelOutput);
        blockStateModelGenerator.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(modelId))
                                .with(NORTH_DEFAULT_HORIZONTAL_ROTATION_OPERATIONS)
                );

        ItemTintSource wireOffTint = ItemModelUtils.constantTint(RedStoneWireBlock.getColorForPower(0));
        blockStateModelGenerator.itemModelOutput.accept(block.asItem(), ItemModelUtils.tintedModel(modelId, wireOffTint, wireOffTint, wireOffTint, wireOffTint));
    }
}
