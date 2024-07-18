package net.replaceitem.integratedcircuit.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Initial schema adding types of first IntegratedCircuit version (Data version 1.19.2)
 */
public class Schema3120 extends IdentifierNormalizingSchema {
    public Schema3120(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.registerSimple(
                map,
                "integrated_circuit:integrated_circuit_block_entity"
        );
        return map;
    }
}
