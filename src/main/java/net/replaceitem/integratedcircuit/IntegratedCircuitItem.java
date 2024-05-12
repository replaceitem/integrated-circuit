package net.replaceitem.integratedcircuit;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class IntegratedCircuitItem extends BlockItem {
    public IntegratedCircuitItem(IntegratedCircuitBlock block) {
        super(block, new Settings());
    }

    @Override
    public Text getName(ItemStack stack) {
        if(!stack.getComponents().isEmpty()) return super.getName(stack);
        return Text.translatable(this.getTranslationKey() + ".empty");
    }
}
