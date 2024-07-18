package net.replaceitem.integratedcircuit.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema704;
import net.replaceitem.integratedcircuit.datafix.CircuitItemFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Schema704.class)
public class Schema704Mixin {
    @Redirect(method = "registerTypes", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V", ordinal = 0))
    private void registerBlockEntityWithoutComponents(Schema instance, boolean recursive, DSL.TypeReference type, Supplier<TypeTemplate> template, Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        instance.registerType(true, TypeReferences.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", IdentifierNormalizingSchema.getIdentifierType(), blockEntityTypes));
    }
    
    @Inject(method = "method_5297", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private static void addBlockEntityItemMappings(CallbackInfoReturnable<ImmutableMap<String, String>> cir, @Local Map<String, String> map) {
        for (String circuitItem : CircuitItemFix.CIRCUIT_ITEMS) {
            map.put(circuitItem, "integrated_circuit:integrated_circuit_block_entity");
        }
    }
}
