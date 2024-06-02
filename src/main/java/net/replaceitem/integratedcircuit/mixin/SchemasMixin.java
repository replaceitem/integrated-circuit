package net.replaceitem.integratedcircuit.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceTypesFix;
import net.minecraft.datafixer.fix.RenameBlockEntityFix;
import net.minecraft.datafixer.fix.WriteAndReadFix;
import net.replaceitem.integratedcircuit.datafix.CircuitBlockEntityFix;
import net.replaceitem.integratedcircuit.datafix.Schema3120;
import net.replaceitem.integratedcircuit.datafix.Schema3800_1;
import net.replaceitem.integratedcircuit.datafix.Schema3800_4;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

@Mixin(Schemas.class)
public abstract class SchemasMixin {
    @Shadow @Final private static BiFunction<Integer, Schema, Schema> EMPTY_IDENTIFIER_NORMALIZE;

    @Shadow
    private static UnaryOperator<String> replacing(String old, String current) {
        return null;
    }

    @Inject(method = "build", at = @At(value = "CONSTANT", args = "intValue=3201"))
    private static void beforeSchema3201(DataFixerBuilder builder, CallbackInfo ci) {
        Schema schema3120 = builder.addSchema(3120, Schema3120::new);
        builder.addFixer(new ChoiceTypesFix(schema3120, "Added Integrated Circuit", TypeReferences.BLOCK_ENTITY));
    }
    @Inject(method = "build", at = @At(value = "CONSTANT", args = "intValue=3803"))
    private static void beforeSchema3803(DataFixerBuilder builder, CallbackInfo ci) {
        Schema schema3800_1 = builder.addSchema(3800, 1, Schema3800_1::new);
        builder.addFixer(RenameBlockEntityFix.create(schema3800_1, "Rename integrated_circuit:integrated_circuit_block_entity to integrated_circuit:integrated_circuit", replacing("integrated_circuit:integrated_circuit_block_entity", "integrated_circuit:integrated_circuit")));
        Schema schema3800_2 = builder.addSchema(3800, 2, EMPTY_IDENTIFIER_NORMALIZE);
        builder.addFixer(new CircuitBlockEntityFix(schema3800_2));
        //Schema schema3800_3 = builder.addSchema(3800, 3, EMPTY_IDENTIFIER_NORMALIZE);
        //builder.addFixer(new CircuitItemFix(schema3800_3));
        Schema schema3800_4 = builder.addSchema(3800, 3, Schema3800_4::new); // TODO remove if not needed
        builder.addFixer(new WriteAndReadFix(schema3800_4, "Inject data component types", TypeReferences.DATA_COMPONENTS));
        
        //TODO
        // items arent converted, because item->BE mapping isnt properly working (should it map to ic:ic or ic:icbe?
    }
}
