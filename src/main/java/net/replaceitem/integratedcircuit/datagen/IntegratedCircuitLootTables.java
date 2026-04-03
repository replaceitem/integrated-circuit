package net.replaceitem.integratedcircuit.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;

import java.util.concurrent.CompletableFuture;

public class IntegratedCircuitLootTables extends FabricBlockLootSubProvider {
    public IntegratedCircuitLootTables(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (IntegratedCircuitBlock block : IntegratedCircuit.Blocks.CIRCUITS) {
            addIntegratedCircuitDrop(block);
        }
    }
    
    private void addIntegratedCircuitDrop(Block block) {
        add(block, LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .when(ExplosionCondition.survivesExplosion())
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                                LootItem.lootTableItem(block)
                                        .apply(
                                                CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                                        .include(DataComponents.CUSTOM_NAME)
                                                        .include(IntegratedCircuit.CIRCUIT_DATA)
                                        )
                        )
        ));
    }
}
