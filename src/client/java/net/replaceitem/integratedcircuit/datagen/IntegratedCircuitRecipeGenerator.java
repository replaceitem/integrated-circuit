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
            .put(IntegratedCircuit.Items.WHITE_INTEGRATED_CIRCUIT     , Items.CONCRETE.white())
            .put(IntegratedCircuit.Items.ORANGE_INTEGRATED_CIRCUIT    , Items.CONCRETE.orange())
            .put(IntegratedCircuit.Items.MAGENTA_INTEGRATED_CIRCUIT   , Items.CONCRETE.magenta())
            .put(IntegratedCircuit.Items.LIGHT_BLUE_INTEGRATED_CIRCUIT, Items.CONCRETE.lightBlue())
            .put(IntegratedCircuit.Items.YELLOW_INTEGRATED_CIRCUIT    , Items.CONCRETE.yellow())
            .put(IntegratedCircuit.Items.LIME_INTEGRATED_CIRCUIT      , Items.CONCRETE.lime())
            .put(IntegratedCircuit.Items.PINK_INTEGRATED_CIRCUIT      , Items.CONCRETE.pink())
            .put(IntegratedCircuit.Items.GRAY_INTEGRATED_CIRCUIT      , Items.CONCRETE.gray())
            .put(IntegratedCircuit.Items.LIGHT_GRAY_INTEGRATED_CIRCUIT, Items.CONCRETE.lightGray())
            .put(IntegratedCircuit.Items.CYAN_INTEGRATED_CIRCUIT      , Items.CONCRETE.cyan())
            .put(IntegratedCircuit.Items.PURPLE_INTEGRATED_CIRCUIT    , Items.CONCRETE.purple())
            .put(IntegratedCircuit.Items.BLUE_INTEGRATED_CIRCUIT      , Items.CONCRETE.blue())
            .put(IntegratedCircuit.Items.BROWN_INTEGRATED_CIRCUIT     , Items.CONCRETE.brown())
            .put(IntegratedCircuit.Items.GREEN_INTEGRATED_CIRCUIT     , Items.CONCRETE.green())
            .put(IntegratedCircuit.Items.RED_INTEGRATED_CIRCUIT       , Items.CONCRETE.red())
            .put(IntegratedCircuit.Items.BLACK_INTEGRATED_CIRCUIT     , Items.CONCRETE.black())
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
                    .define('T', Items.DYED_TERRACOTTA.black())
                    .define('R', Items.REDSTONE)
                    .define('Q', Items.QUARTZ)
                    .define('C', baseItem)
                    .unlockedBy(getHasName(Items.QUARTZ), has(Items.QUARTZ))
                    .save(output);
        }
        
    }
    
    private void offerDyeingRecipes() {
        offerDyeingRecipe(Items.DYE.white(), IntegratedCircuit.Items.WHITE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.orange(), IntegratedCircuit.Items.ORANGE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.magenta(), IntegratedCircuit.Items.MAGENTA_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.lightBlue(), IntegratedCircuit.Items.LIGHT_BLUE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.yellow(), IntegratedCircuit.Items.YELLOW_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.lime(), IntegratedCircuit.Items.LIME_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.pink(), IntegratedCircuit.Items.PINK_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.gray(), IntegratedCircuit.Items.GRAY_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.lightGray(), IntegratedCircuit.Items.LIGHT_GRAY_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.cyan(), IntegratedCircuit.Items.CYAN_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.purple(), IntegratedCircuit.Items.PURPLE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.blue(), IntegratedCircuit.Items.BLUE_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.brown(), IntegratedCircuit.Items.BROWN_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.green(), IntegratedCircuit.Items.GREEN_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.red(), IntegratedCircuit.Items.RED_INTEGRATED_CIRCUIT);
        offerDyeingRecipe(Items.DYE.black(), IntegratedCircuit.Items.BLACK_INTEGRATED_CIRCUIT);
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
