package net.replaceitem.integratedcircuit.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.datafixer.schema.Schema704;
import net.replaceitem.integratedcircuit.datafix.CircuitItemFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Schema704.class)
public class Schema704Mixin {
    @Inject(method = "method_5297", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private static void addBlockEntityItemMappings(CallbackInfoReturnable<ImmutableMap<String, String>> cir, @Local Map<String, String> map) {
        for (String circuitItem : CircuitItemFix.CIRCUIT_ITEMS) {
            map.put(circuitItem, "integrated_circuit:integrated_circuit_block_entity");
        }
    }
}
