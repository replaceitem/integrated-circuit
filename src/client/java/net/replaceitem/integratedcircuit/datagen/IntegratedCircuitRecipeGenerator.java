package net.replaceitem.integratedcircuit.datagen;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.TransmuteRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IntegratedCircuitRecipeGenerator extends RecipeProvider {

    protected IntegratedCircuitRecipeGenerator(HolderLookup.Provider registries, RecipeOutput exporter) {
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
            shaped(RecipeCategory.REDSTONE, item)
                    .group(GROUP)
                    .pattern(" T ")
                    .pattern("RQR")
                    .pattern("CCC")
                    .define('T', Items.BLACK_TERRACOTTA)
                    .define('R', Items.REDSTONE)
                    .define('Q', Items.QUARTZ)
                    .define('C', baseItem)
                    .unlockedBy(getHasName(Items.QUARTZ), has(Items.QUARTZ))
                    .save(output);
        }
        
    }
    
    private void offerDyeingRecipes() {
        offerDyeingRecipe(Items.WHITE_DYE, IntegratedCircuit.Items.WHITE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.ORANGE_DYE, IntegratedCircuit.Items.ORANGE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.MAGENTA_DYE, IntegratedCircuit.Items.MAGENTA_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.LIGHT_BLUE_DYE, IntegratedCircuit.Items.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.YELLOW_DYE, IntegratedCircuit.Items.YELLOW_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.LIME_DYE, IntegratedCircuit.Items.LIME_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.PINK_DYE, IntegratedCircuit.Items.PINK_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.GRAY_DYE, IntegratedCircuit.Items.GRAY_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.LIGHT_GRAY_DYE, IntegratedCircuit.Items.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.CYAN_DYE, IntegratedCircuit.Items.CYAN_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.PURPLE_DYE, IntegratedCircuit.Items.PURPLE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.BLUE_DYE, IntegratedCircuit.Items.BLUE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.BROWN_DYE, IntegratedCircuit.Items.BROWN_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.GREEN_DYE, IntegratedCircuit.Items.GREEN_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.RED_DYE, IntegratedCircuit.Items.RED_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.BLACK_DYE, IntegratedCircuit.Items.BLACK_INTEGRATED_CIRCUIT);
    }

    private void offerDyeingRecipe(Item dyeItem, Item circuitItem) {
        Ingredient ingredient = tag(IntegratedCircuit.Tags.DYEABLE_INTEGRATED_CIRCUITS_ITEM_TAG);
        TransmuteRecipeBuilder.transmute(RecipeCategory.REDSTONE, ingredient, Ingredient.of(dyeItem), circuitItem)
                .group("integrated_circuit_dye")
                .unlockedBy(getHasName(dyeItem), this.has(dyeItem))
                .save(this.output, ResourceKey.create(Registries.RECIPE, IntegratedCircuit.id("dye_" + getItemName(circuitItem))));
    }

    @Override
    public void buildRecipes() {
        offerCircuitRecipes();
        offerDyeingRecipes();
    }

    static class Provider extends FabricRecipeProvider {
        public Provider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput recipeExporter) {
            return new IntegratedCircuitRecipeGenerator(wrapperLookup, recipeExporter);
        }

        @Override
        public String getName() {
            return "Integrated Circuit recipes";
        }
    }
}
