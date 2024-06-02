package net.replaceitem.integratedcircuit.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;

import java.util.Map;
import java.util.function.Supplier;

public class Schema3800_4 extends Schema {
    public Schema3800_4(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        /*schema.registerType(
                true,
                TypeReferences.BLOCK_ENTITY,
                () -> DSL.optionalFields("components", TypeReferences.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", IdentifierNormalizingSchema.getIdentifierType(), blockEntityTypes))
        );*/
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, DSL::remainder);
    }
}
