package net.replaceitem.integratedcircuit.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema99;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.Supplier;

@Debug(export = true)
@Mixin(Schema99.class)
public abstract class Schema99Mixin {
    @Redirect(method = "registerTypes", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V", ordinal = 3))
    private void registerBlockEntityWithoutComponents(Schema instance, boolean recursive, DSL.TypeReference type, Supplier<TypeTemplate> template, Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        instance.registerType(true, TypeReferences.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), blockEntityTypes));
    }
    
    /*
    @ModifyArg(method = "registerTypes", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V", ordinal = 27), index = 2)
    private Supplier<TypeTemplate> registerDataComponentsAsEmpty(Supplier<TypeTemplate> template) {
        return DSL::remainder;
    }*/
}
