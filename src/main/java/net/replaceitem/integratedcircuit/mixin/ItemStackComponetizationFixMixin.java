package net.replaceitem.integratedcircuit.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.replaceitem.integratedcircuit.datafix.CircuitBlockEntityFix;
import net.replaceitem.integratedcircuit.datafix.CircuitItemFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponetizationFixMixin {
    
    @Inject(method = "fixStack", at = @At("HEAD"))
    private static void fixIntegratedCircuitBlockEntity(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if (data.itemMatches(CircuitItemFix.CIRCUIT_ITEMS)) {

            data.applyFixer("BlockEntityTag", false, blockEntityTag -> {
                // check if not updated yet: might not be updated by CircuitBlockEntityFix yet, since we did not serialize the BE id in old IC versions, which is normally done when doing ctrl + pick block
                if(blockEntityTag.get("id").result().isEmpty() && blockEntityTag.get("components").result().map(d -> d.asListOpt(Function.identity()).isSuccess()).orElse(false) && blockEntityTag.get("circuit").result().isEmpty()) {
                    return CircuitBlockEntityFix.fixCircuitBlockEntityData(blockEntityTag);
                }
                return blockEntityTag;
            });
            
            data.applyFixer("BlockEntityTag", false, blockEntityTag -> {
                data.setComponent("integrated_circuit:circuit", blockEntityTag.get("circuit"));
                return blockEntityTag.remove("circuit");
            });
        }
    }
}
