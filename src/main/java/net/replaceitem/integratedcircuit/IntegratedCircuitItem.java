package net.replaceitem.integratedcircuit;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import static net.replaceitem.integratedcircuit.IntegratedCircuit.Items;

public class IntegratedCircuitItem extends BlockItem {
    public IntegratedCircuitItem(IntegratedCircuitBlock block, Item.Settings settings) {
        super(block, settings);
    }

    public static Item fromColor(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_INTEGRATED_CIRCUIT;
            case ORANGE -> Items.ORANGE_INTEGRATED_CIRCUIT;
            case MAGENTA -> Items.MAGENTA_INTEGRATED_CIRCUIT;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_INTEGRATED_CIRCUIT;
            case YELLOW -> Items.YELLOW_INTEGRATED_CIRCUIT;
            case LIME -> Items.LIME_INTEGRATED_CIRCUIT;
            case PINK -> Items.PINK_INTEGRATED_CIRCUIT;
            case GRAY -> Items.GRAY_INTEGRATED_CIRCUIT;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_INTEGRATED_CIRCUIT;
            case CYAN -> Items.CYAN_INTEGRATED_CIRCUIT;
            case BLUE -> Items.BLUE_INTEGRATED_CIRCUIT;
            case BROWN -> Items.BROWN_INTEGRATED_CIRCUIT;
            case GREEN -> Items.GREEN_INTEGRATED_CIRCUIT;
            case RED -> Items.RED_INTEGRATED_CIRCUIT;
            case BLACK -> Items.BLACK_INTEGRATED_CIRCUIT;
            case PURPLE -> Items.PURPLE_INTEGRATED_CIRCUIT;
        };
    }

    @Override
    public Text getName(ItemStack stack) {
        if(stack.getComponents().contains(IntegratedCircuit.CIRCUIT_DATA)) return super.getName(stack);
        return Text.translatable(this.getTranslationKey() + ".empty");
    }
}
