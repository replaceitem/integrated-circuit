package net.replaceitem.integratedcircuit.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.replaceitem.integratedcircuit.datafix.IntegratedCircuitDatafixers;

import java.util.Map;
import java.util.function.Supplier;

public class Schema3838 extends NamespacedSchema {
    public Schema3838(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        var map = super.registerBlockEntities(schema);
        schema.register(
                map,
                "integrated_circuit:integrated_circuit",
                () -> DSL.optionalFields(
                        "circuit",
                        IntegratedCircuitDatafixers.References.CIRCUIT.in(schema)
                )
        );
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(
                false,
                IntegratedCircuitDatafixers.References.CIRCUIT,
                () -> DSL.optionalFields(
                        "section",
                        DSL.fields(
                                "component_states",
                                DSL.optionalFields(
                                        "palette",
                                        DSL.list(
                                                IntegratedCircuitDatafixers.References.COMPONENT_STATE.in(schema)
                                        )
                                )
                        ),
                        "ports",
                        DSL.list(IntegratedCircuitDatafixers.References.COMPONENT_STATE.in(schema))
                )
        );
        schema.registerType(false, IntegratedCircuitDatafixers.References.COMPONENT_STATE, DSL::remainder);
    }
}
