package net.replaceitem.integratedcircuit.mixin;

import net.minecraft.world.BlockRenderView;
import net.minecraft.world.RedstoneView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockRenderView.class)
public interface BlockRenderViewMixin extends RedstoneView {

}
