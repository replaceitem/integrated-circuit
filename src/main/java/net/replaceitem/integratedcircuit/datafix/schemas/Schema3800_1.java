package net.replaceitem.integratedcircuit.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Renamed circuit block entity
 */
public class Schema3800_1 extends NamespacedSchema {
    public Schema3800_1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        map.put("integrated_circuit:integrated_circuit", map.remove("integrated_circuit:integrated_circuit_block_entity"));
        return map;
    }
}
