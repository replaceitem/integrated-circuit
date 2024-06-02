package net.replaceitem.integratedcircuit.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

import java.util.Optional;
import java.util.Set;

public class CircuitItemFix extends DataFix {
    public static final Set<String> CIRCUIT_ITEMS = Set.of(
            "integrated_circuit:integrated_circuit",
            "integrated_circuit:white_integrated_circuit",
            "integrated_circuit:orange_integrated_circuit",
            "integrated_circuit:magenta_integrated_circuit",
            "integrated_circuit:light_blue_integrated_circuit",
            "integrated_circuit:yellow_integrated_circuit",
            "integrated_circuit:lime_integrated_circuit",
            "integrated_circuit:pink_integrated_circuit",
            "integrated_circuit:gray_integrated_circuit",
            "integrated_circuit:light_gray_integrated_circuit",
            "integrated_circuit:cyan_integrated_circuit",
            "integrated_circuit:purple_integrated_circuit",
            "integrated_circuit:blue_integrated_circuit",
            "integrated_circuit:brown_integrated_circuit",
            "integrated_circuit:green_integrated_circuit",
            "integrated_circuit:red_integrated_circuit",
            "integrated_circuit:black_integrated_circuit"
    );
    
    public CircuitItemFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder<Pair<String, String>> idFinder = DSL.fieldFinder(
                "id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType())
        );
        OpticFinder<?> opticFinder = type.findField("tag");
        
        return this.fixTypeEverywhereTyped("CircuitItemFix", type, typed -> typed.updateTyped(opticFinder, typedx -> {
            Optional<Pair<String, String>> optionalId = typed.getOptional(idFinder);
            if (optionalId.isPresent()) {
                String id = optionalId.get().getSecond();
                if (CIRCUIT_ITEMS.contains(id)) {
                    Typed<?> tag = typed.getOrCreateTyped(opticFinder);
                    return tag.update(DSL.remainderFinder(), this::convert);
                }
            }
            return typed;
        }));
    }

    private Dynamic<?> convert(Dynamic<?> dynamic) {
        return dynamic.renameAndFixField("BlockEntityTag", "circuit", dynamic1 ->
                CircuitBlockEntityFix.convertToCircuit(dynamic1).orElseGet(dynamic1::emptyMap)
        );
    }
}
