package net.replaceitem.integratedcircuit;

import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class IntegratedCircuitCloningRecipe extends SpecialCraftingRecipe {
    public IntegratedCircuitCloningRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        int sourceIndex = -1;
        int destIndex = -1;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return false;

            if(stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
                if(sourceIndex != -1) return false;// Only one should have data
                sourceIndex = i;
            } else {
               if(destIndex != -1) return false;
               destIndex = i;
            }
        }

        return sourceIndex != -1 && destIndex != -1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup) {
        int sourceIndex = -1;
        int destIndex = -1;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return ItemStack.EMPTY;

            if(stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
                if(sourceIndex != -1) return ItemStack.EMPTY; // Only one should have NBT data
                sourceIndex = i;
            } else {
                if(destIndex != -1) return ItemStack.EMPTY;
                destIndex = i;
            }
        }

        if(sourceIndex != -1 && destIndex != -1) {
            ItemStack source = input.getStackInSlot(sourceIndex);
            ItemStack dest = input.getStackInSlot(destIndex);

            ItemStack craftedStack = dest.copyWithCount(1);
            craftedStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, source.get(DataComponentTypes.BLOCK_ENTITY_DATA));
            return craftedStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return remainder;

            if(stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
                remainder.set(i, stack.copyWithCount(1));
                return remainder;
            }
        }
        return remainder;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return IntegratedCircuit.CIRCUIT_CLONING_RECIPE;
    }
}