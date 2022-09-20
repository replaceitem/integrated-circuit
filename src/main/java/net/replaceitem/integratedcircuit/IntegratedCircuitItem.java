package net.replaceitem.integratedcircuit;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegratedCircuitItem extends BlockItem {
    public IntegratedCircuitItem() {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK,  new FabricItemSettings().group(ItemGroup.REDSTONE));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        //super.appendTooltip(stack, world, tooltip, context);
        NbtCompound blockEntityNbt = BlockItem.getBlockEntityNbt(stack);
        if(blockEntityNbt != null && blockEntityNbt.contains("CustomName", NbtElement.STRING_TYPE)) {
            tooltip.add(Text.Serializer.fromJson(blockEntityNbt.getString("CustomName")));
        }
    }


}
