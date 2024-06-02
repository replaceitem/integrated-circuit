package net.replaceitem.integratedcircuit.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

import java.util.Map;
import java.util.Optional;

public class CircuitBlockEntityFix extends ChoiceFix {
    public CircuitBlockEntityFix(Schema schema) {
        super(schema, false, "CircuitBlockEntityFix", TypeReferences.BLOCK_ENTITY, "integrated_circuit:integrated_circuit");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputType) {
        return inputType.update(DSL.remainderFinder(), CircuitBlockEntityFix::fixCircuitBlockEntityData);
    }

    public static Dynamic<?> fixCircuitBlockEntityData(Dynamic<?> dynamic) {
        Optional<Dynamic<?>> fixedData = convertToCircuit(dynamic);
        dynamic = dynamic.remove("components").remove("ports").remove("tickScheduler").remove("outputStrengths");
        return fixedData.isPresent() ? dynamic.set("circuit", fixedData.get()) : dynamic;
    }

    public static Optional<Dynamic<?>> convertToCircuit(Dynamic<?> dynamic) {
        Optional<Dynamic<?>> sectionDynamic = dynamic.get("components").result()
                .flatMap(CircuitFixer::fixCircuitSection);
        Optional<Dynamic<?>> portsDynamic = dynamic.get("ports").result()
                .flatMap(CircuitFixer::fixPortStates);
        Optional<Dynamic<?>> tickSchedulerDynamic = dynamic.get("tickScheduler").result()
                .flatMap(CircuitFixer::fixTickScheduler);
        if(sectionDynamic.isEmpty() || portsDynamic.isEmpty() || tickSchedulerDynamic.isEmpty()) return Optional.empty();

        return Optional.of(dynamic.createMap(Map.of(
                dynamic.createString("section"), sectionDynamic.get(),
                dynamic.createString("ports"), portsDynamic.get(),
                dynamic.createString("tickScheduler"), tickSchedulerDynamic.get()
        )));
    }
}
