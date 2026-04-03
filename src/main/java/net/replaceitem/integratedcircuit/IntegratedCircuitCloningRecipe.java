package net.replaceitem.integratedcircuit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class IntegratedCircuitCloningRecipe extends CustomRecipe {
    
    public static DataComponentType<CustomData> CLONED_COMPONENT = IntegratedCircuit.CIRCUIT_DATA;

    public static final MapCodec<IntegratedCircuitCloningRecipe> MAP_CODEC = MapCodec.unit(IntegratedCircuitCloningRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntegratedCircuitCloningRecipe> STREAM_CODEC = StreamCodec.unit(
            new IntegratedCircuitCloningRecipe()
    );

    public static final RecipeSerializer<IntegratedCircuitCloningRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, Level world) {
        int sourceIndex = -1;
        int destIndex = -1;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return false;

            if(stack.has(CLONED_COMPONENT)) {
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
    public ItemStack assemble(CraftingInput input) {
        int sourceIndex = -1;
        int destIndex = -1;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return ItemStack.EMPTY;

            if(stack.has(CLONED_COMPONENT)) {
                if(sourceIndex != -1) return ItemStack.EMPTY; // Only one should have NBT data
                sourceIndex = i;
            } else {
                if(destIndex != -1) return ItemStack.EMPTY;
                destIndex = i;
            }
        }

        if(sourceIndex != -1 && destIndex != -1) {
            ItemStack source = input.getItem(sourceIndex);
            ItemStack dest = input.getItem(destIndex);

            ItemStack craftedStack = dest.copyWithCount(1);
            craftedStack.set(CLONED_COMPONENT, source.get(CLONED_COMPONENT));
            craftedStack.set(DataComponents.CUSTOM_NAME, source.get(DataComponents.CUSTOM_NAME));
            return craftedStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainder = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if(stack.isEmpty()) continue;
            if(!(stack.getItem() instanceof IntegratedCircuitItem)) return remainder;

            if(stack.has(CLONED_COMPONENT)) {
                remainder.set(i, stack.copyWithCount(1));
                return remainder;
            }
        }
        return remainder;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}