package net.replaceitem.integratedcircuit;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegratedCircuitItem extends BlockItem {
    public static final Text NAME_EMPTY = Text.translatable("integrated_circuit.empty_circuit");
    
    public IntegratedCircuitItem() {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK,  new FabricItemSettings());
    }

    @Override
    public Text getName(ItemStack stack) {
        return stack.hasNbt() ? super.getName(stack) : NAME_EMPTY;
    }
}
