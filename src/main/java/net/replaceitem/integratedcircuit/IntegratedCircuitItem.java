package net.replaceitem.integratedcircuit;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class IntegratedCircuitItem extends BlockItem {
    public IntegratedCircuitItem(IntegratedCircuitBlock block) {
        super(block, new FabricItemSettings());
    }

    @Override
    public Text getName(ItemStack stack) {
        if(stack.hasNbt()) {
            return super.getName(stack);
        }
        return Text.translatable(this.getTranslationKey() + ".empty");
    }
}
