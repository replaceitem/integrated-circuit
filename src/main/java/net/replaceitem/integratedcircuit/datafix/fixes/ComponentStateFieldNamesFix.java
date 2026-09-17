package net.replaceitem.integratedcircuit.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.replaceitem.integratedcircuit.datafix.IntegratedCircuitDatafixers;

/**
 * Same thing as in {@link net.minecraft.util.datafix.fixes.BlockStateFieldNamesFix}, since component states use the same codec, this needs to be changed here too.
 * Component states don't use the {@link net.minecraft.util.datafix.fixes.References#BLOCK_STATE} reference though (might have too many consequences), so the same fix is applied to component states here.
 */
public class ComponentStateFieldNamesFix extends DataFix {
    public ComponentStateFieldNamesFix(final Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
                "ComponentStateFieldNamesFix",
                this.getInputSchema().getType(IntegratedCircuitDatafixers.References.COMPONENT_STATE),
                input -> input.update(
                        DSL.remainderFinder(),
                        remainder -> remainder
                                .renameField("Name", "id")
                                .renameField("Properties", "properties")
                )
        );
    }
}
