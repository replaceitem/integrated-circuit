package net.replaceitem.integratedcircuit.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;

import java.util.concurrent.CompletableFuture;

public class IntegratedCircuitLootTables extends FabricBlockLootTableProvider {
    public IntegratedCircuitLootTables(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (IntegratedCircuitBlock block : IntegratedCircuit.Blocks.CIRCUITS) {
            addIntegratedCircuitDrop(block);
        }
    }
    
    private void addIntegratedCircuitDrop(Block block) {
        addDrop(block, LootTable.builder().pool(
                LootPool.builder()
                        .conditionally(SurvivesExplosionLootCondition.builder())
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(
                                ItemEntry.builder(block)
                                        .apply(
                                                CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY)
                                                        .include(DataComponentTypes.CUSTOM_NAME)
                                                        .include(IntegratedCircuit.CIRCUIT_DATA)
                                        )
                        )
        ));
    }
}
