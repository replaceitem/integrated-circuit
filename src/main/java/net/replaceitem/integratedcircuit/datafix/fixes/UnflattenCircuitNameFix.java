package net.replaceitem.integratedcircuit.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.replaceitem.integratedcircuit.IntegratedCircuit;


public class UnflattenCircuitNameFix extends NamedEntityFix {
    public UnflattenCircuitNameFix(Schema outputSchema, String blockEntityId) {
        super(outputSchema, true, "UnflattenCircuitNameFix" + blockEntityId, References.BLOCK_ENTITY, blockEntityId);
    }

    public Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.update("CustomName", dynamic1 -> dynamic1.asString()
                .mapOrElse(
                        s -> convertText(dynamic1.getOps(), s),
                        stringError -> dynamic1
                )
        );
    }

    @Override
    protected Typed<?> fix(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fix);
    }


    private static <T> Dynamic<T> convertText(DynamicOps<T> dynamicOps, String string) {
        try {
            JsonElement jsonElement = LenientJsonParser.parse(string);
            if (!jsonElement.isJsonNull()) {
                return new Dynamic<>(dynamicOps, JsonOps.INSTANCE.convertTo(dynamicOps, jsonElement));
            }
        } catch (Exception var3) {
            IntegratedCircuit.LOGGER.error("Failed to unflatten text component json: {}", string, var3);
        }

        return new Dynamic<>(dynamicOps, dynamicOps.createString(string));
    }
}