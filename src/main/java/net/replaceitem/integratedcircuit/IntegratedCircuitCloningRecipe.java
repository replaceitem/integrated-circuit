package net.replaceitem.integratedcircuit;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class IntegratedCircuitCloningRecipe extends SpecialCraftingRecipe {
    public IntegratedCircuitCloningRecipe(Identifier identifier, CraftingRecipeCategory category) {
        super(identifier, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int sourceIndex = -1;
        int destIndex = -1;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return false;

            if(stack.hasNbt() && stack.getNbt().contains(BlockItem.BLOCK_ENTITY_TAG_KEY)) {
                if(sourceIndex != -1) return false;// Only one should have NBT data
                sourceIndex = i;
            } else if(!stack.hasNbt()) {
               if(destIndex != -1) return false;
               destIndex = i;
            }
        }

        return sourceIndex != -1 && destIndex != -1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        int sourceIndex = -1;
        int destIndex = -1;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return ItemStack.EMPTY;

            if(stack.hasNbt() && stack.getNbt().contains(BlockItem.BLOCK_ENTITY_TAG_KEY)) {
                if(sourceIndex != -1) return ItemStack.EMPTY; // Only one should have NBT data
                sourceIndex = i;
            } else if(!stack.hasNbt()) {
                if(destIndex != -1) return ItemStack.EMPTY;
                destIndex = i;
            }
        }

        if(sourceIndex != -1 && destIndex != -1) {
            ItemStack source = inventory.getStack(sourceIndex);
            ItemStack dest = inventory.getStack(destIndex);

            ItemStack craftedStack = dest.copyWithCount(1);
            craftedStack.setNbt(source.getNbt());
            return craftedStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return remainder;

            if(stack.hasNbt() && stack.getNbt().contains(BlockItem.BLOCK_ENTITY_TAG_KEY)) {
                remainder.set(i, stack.copyWithCount(1));
                return remainder;
            }
        }
        return remainder;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IntegratedCircuit.CIRCUIT_CLONING_RECIPE;
    }
}