package net.replaceitem.integratedcircuit.mixin;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneWireBlockAccessor {
    @Accessor
    static Vec3d[] getCOLORS() {
        throw new UnsupportedOperationException();
    }
}
