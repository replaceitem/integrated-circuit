package net.replaceitem.integratedcircuit;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class IntegratedCircuitCloningRecipe extends SpecialCraftingRecipe {
    public IntegratedCircuitCloningRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int sourceIndex = -1;
        int destinationIndex = -1;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if(stack.isEmpty()) continue;
            if(!stack.isOf(IntegratedCircuit.INTEGRATED_CIRCUIT_ITEM)) return false;
            if(sourceIndex == -1 && stack.getCount() == 1 && stack.hasNbt() && stack.getNbt().contains(BlockItem.BLOCK_ENTITY_TAG_KEY)) {
                sourceIndex = i;
                continue;
            }
            if(destinationIndex == -1 && !stack.hasNbt()) {
                destinationIndex = i;
                continue;
            }
            return false;
        }
        
        return sourceIndex != -1 && destinationIndex != -1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory craftingInventory, DynamicRegistryManager registryManager) {
        int sourceIndex = -1;
        int destinationIndex = -1;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if(stack.isEmpty()) continue;
            if(!stack.isOf(IntegratedCircuit.INTEGRATED_CIRCUIT_ITEM)) return ItemStack.EMPTY;
            if(sourceIndex == -1 && stack.getCount() == 1 && stack.hasNbt() && stack.getNbt().contains(BlockItem.BLOCK_ENTITY_TAG_KEY)) {
                sourceIndex = i;
                continue;
            }
            if(destinationIndex == -1 && !stack.hasNbt()) {
                destinationIndex = i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = craftingInventory.getStack(sourceIndex);
        ItemStack craftedStack = IntegratedCircuit.INTEGRATED_CIRCUIT_ITEM.getDefaultStack();
        craftedStack.setNbt(sourceStack.getNbt().copy());
        return craftedStack;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory craftingInventory) {
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(craftingInventory.size(), ItemStack.EMPTY);
        int sourceIndex = -1;
        int destinationIndex = -1;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if(stack.isEmpty()) continue;
            if(!stack.isOf(IntegratedCircuit.INTEGRATED_CIRCUIT_ITEM)) return remainder;
            if(sourceIndex == -1 && stack.getCount() == 1 && stack.hasNbt() && stack.getNbt().contains(BlockItem.BLOCK_ENTITY_TAG_KEY)) {
                sourceIndex = i;
                continue;
            }
            if(destinationIndex == -1 && !stack.hasNbt()) {
                destinationIndex = i;
                continue;
            }
            return remainder;
        }
        
        remainder.set(sourceIndex, craftingInventory.getStack(sourceIndex).copy());
        
        return remainder;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IntegratedCircuit.CIRCUIT_CLONING;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }
}