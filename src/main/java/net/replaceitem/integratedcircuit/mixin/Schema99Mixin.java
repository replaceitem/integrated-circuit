package net.replaceitem.integratedcircuit.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.schema.Schema99;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Mixin(Schema99.class)
public abstract class Schema99Mixin {
    @ModifyArg(method = "registerTypes", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V", ordinal = 27), index = 2)
    private Supplier<TypeTemplate> registerDataComponentsAsEmpty(Supplier<TypeTemplate> template) {
        return DSL::emptyPart;
    }
}
