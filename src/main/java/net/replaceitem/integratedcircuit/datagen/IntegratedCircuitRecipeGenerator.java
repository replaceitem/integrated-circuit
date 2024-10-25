package net.replaceitem.integratedcircuit.datagen;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.data.server.recipe.TransmuteRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitItem;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IntegratedCircuitRecipeGenerator extends RecipeGenerator {

    protected IntegratedCircuitRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
    }
    
    private static final String GROUP = IntegratedCircuit.id("integrated_circuit").toString();

    private static final Map<Item, Item> BASE_ITEMS = ImmutableMap.<Item,Item>builder()
            .put(IntegratedCircuit.Items.INTEGRATED_CIRCUIT           , Items.STONE)
            .put(IntegratedCircuit.Items.WHITE_INTEGRATED_CIRCUIT     , Items.WHITE_CONCRETE)
            .put(IntegratedCircuit.Items.ORANGE_INTEGRATED_CIRCUIT    , Items.ORANGE_CONCRETE)
            .put(IntegratedCircuit.Items.MAGENTA_INTEGRATED_CIRCUIT   , Items.MAGENTA_CONCRETE)
            .put(IntegratedCircuit.Items.LIGHT_BLUE_INTEGRATED_CIRCUIT, Items.LIGHT_BLUE_CONCRETE)
            .put(IntegratedCircuit.Items.YELLOW_INTEGRATED_CIRCUIT    , Items.YELLOW_CONCRETE)
            .put(IntegratedCircuit.Items.LIME_INTEGRATED_CIRCUIT      , Items.LIME_CONCRETE)
            .put(IntegratedCircuit.Items.PINK_INTEGRATED_CIRCUIT      , Items.PINK_CONCRETE)
            .put(IntegratedCircuit.Items.GRAY_INTEGRATED_CIRCUIT      , Items.GRAY_CONCRETE)
            .put(IntegratedCircuit.Items.LIGHT_GRAY_INTEGRATED_CIRCUIT, Items.LIGHT_GRAY_CONCRETE)
            .put(IntegratedCircuit.Items.CYAN_INTEGRATED_CIRCUIT      , Items.CYAN_CONCRETE)
            .put(IntegratedCircuit.Items.PURPLE_INTEGRATED_CIRCUIT    , Items.PURPLE_CONCRETE)
            .put(IntegratedCircuit.Items.BLUE_INTEGRATED_CIRCUIT      , Items.BLUE_CONCRETE)
            .put(IntegratedCircuit.Items.BROWN_INTEGRATED_CIRCUIT     , Items.BROWN_CONCRETE)
            .put(IntegratedCircuit.Items.GREEN_INTEGRATED_CIRCUIT     , Items.GREEN_CONCRETE)
            .put(IntegratedCircuit.Items.RED_INTEGRATED_CIRCUIT       , Items.RED_CONCRETE)
            .put(IntegratedCircuit.Items.BLACK_INTEGRATED_CIRCUIT     , Items.BLACK_CONCRETE)
            .build();

    private void offerCircuitRecipes() {
        for (Map.Entry<Item, Item> circuitBaseEntry : BASE_ITEMS.entrySet()) {
            Item item = circuitBaseEntry.getKey();
            Item baseItem = circuitBaseEntry.getValue();
            createShaped(RecipeCategory.REDSTONE, item)
                    .group(GROUP)
                    .pattern(" T ")
                    .pattern("RQR")
                    .pattern("CCC")
                    .input('T', Items.BLACK_TERRACOTTA)
                    .input('R', Items.REDSTONE)
                    .input('Q', Items.QUARTZ)
                    .input('C', baseItem)
                    .criterion(hasItem(Items.QUARTZ), conditionsFromItem(Items.QUARTZ))
                    .offerTo(exporter);
        }
        
    }
    
    private void offerDyeingRecipes() {
        Ingredient ingredient = ingredientFromTag(IntegratedCircuit.Tags.DYEABLE_INTEGRATED_CIRCUITS_ITEM_TAG);
        for (DyeColor dyeColor : DyeColor.values()) {
            DyeItem dyeItem = DyeItem.byColor(dyeColor);
            Item circuitItem = IntegratedCircuitItem.fromColor(dyeColor);
            TransmuteRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ingredient, Ingredient.ofItem(dyeItem), circuitItem)
                    .group("integrated_circuit_dye")
                    .criterion(hasItem(dyeItem), this.conditionsFromItem(dyeItem))
                    .offerTo(this.exporter, RegistryKey.of(RegistryKeys.RECIPE, IntegratedCircuit.id("dye_" + getItemPath(circuitItem))));
        }
    }

    @Override
    public void generate() {
        offerCircuitRecipes();
        offerDyeingRecipes();
    }

    static class Provider extends FabricRecipeProvider {
        public Provider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
            return new IntegratedCircuitRecipeGenerator(wrapperLookup, recipeExporter);
        }

        @Override
        public String getName() {
            return "Integrated Circuit recipes";
        }
    }
}
