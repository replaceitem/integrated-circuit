package net.replaceitem.integratedcircuit.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Schema1460.class)
public class Schema1460Mixin {
    @Redirect(method = "registerTypes", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V", ordinal = 4))
    private void registerBlockEntityWithoutComponents(Schema instance, boolean recursive, DSL.TypeReference type, Supplier<TypeTemplate> template, Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        instance.registerType(true, TypeReferences.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", IdentifierNormalizingSchema.getIdentifierType(), blockEntityTypes));
    }
    /*
    @ModifyArg(method = "registerTypes", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V", ordinal = 34), index = 2)
    private Supplier<TypeTemplate> registerDataComponentsAsEmpty(Supplier<TypeTemplate> template) {
        return DSL::remainder;
    }*/
}
