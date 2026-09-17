package net.replaceitem.integratedcircuit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.BlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.filefix.FileFixerUpper;
import net.replaceitem.integratedcircuit.datafix.fixes.ComponentStateFieldNamesFix;
import net.replaceitem.integratedcircuit.datafix.fixes.UnflattenCircuitNameFix;
import net.replaceitem.integratedcircuit.datafix.schemas.Schema3120;
import net.replaceitem.integratedcircuit.datafix.schemas.Schema3800_1;
import net.replaceitem.integratedcircuit.datafix.schemas.Schema3838;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.UnaryOperator;

@Mixin(DataFixers.class)
public abstract class SchemasMixin {
    @Shadow private static UnaryOperator<String> createRenamer(String from, String to) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "addFixers", at = @At(value = "CONSTANT", args = "intValue=3201"))
    private static void beforeSchema3201(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci) {
        Schema schema3120 = fixerUpper.addSchema(3120, Schema3120::new);
        fixerUpper.addFixer(new AddNewChoices(schema3120, "Added Integrated Circuit", References.BLOCK_ENTITY));
    }

    @Inject(method = "addFixers", at = @At(value = "CONSTANT", args = "intValue=3803"))
    private static void beforeSchema3803(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci) {
        Schema schema3800_1 = fixerUpper.addSchema(3800, 1, Schema3800_1::new);
        fixerUpper.addFixer(BlockEntityRenameFix.create(schema3800_1, "Rename integrated_circuit:integrated_circuit_block_entity to integrated_circuit:integrated_circuit", createRenamer("integrated_circuit:integrated_circuit_block_entity", "integrated_circuit:integrated_circuit")));
    }

    @Inject(method = "addFixers", at = @At(value = "CONSTANT", args = "intValue=3938"))
    private static void beforeSchema3938(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci) {
        // 1.20.6, introduction of using PalettedContainer for circuit storage
        // migration wasn't done using DF, so this schema has no fixers
        fixerUpper.addSchema(3838, Schema3838::new);
    }

    // 1.21.5 -> 1.21.6
    @Inject(method = "addFixers", at = @At(value = "CONSTANT", args = "intValue=4424"))
    private static void beforeSchema4424(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci) {
        Schema schema4421_1 = fixerUpper.addSchema(4421, 1, NamespacedSchema::new);
        fixerUpper.addFixer(new UnflattenCircuitNameFix(schema4421_1, "integrated_circuit:integrated_circuit"));
    }

    // At the same point where BlockStateFieldNamesFix is applied
    @Inject(method = "addFixers", at = @At(value = "CONSTANT", args = "intValue=5007"))
    private static void beforeSchema5007(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci, @Local(name = "v5006") Schema v5006) {
        fixerUpper.addFixer(new ComponentStateFieldNamesFix(v5006));
    }
}
