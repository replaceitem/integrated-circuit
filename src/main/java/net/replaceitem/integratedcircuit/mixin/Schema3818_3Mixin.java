package net.replaceitem.integratedcircuit.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema3818_3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Schema3818_3.class)
public class Schema3818_3Mixin {
    @Inject(method = "registerTypes", at = @At("TAIL"))
    private void registerBlockEntityWithDataComponents(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes, CallbackInfo ci) {
        schema.registerType(
                true,
                TypeReferences.BLOCK_ENTITY,
                () -> DSL.optionalFields("components", TypeReferences.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", IdentifierNormalizingSchema.getIdentifierType(), blockEntityTypes))
        );
    }
}
