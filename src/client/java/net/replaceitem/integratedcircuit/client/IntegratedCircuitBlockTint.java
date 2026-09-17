package net.replaceitem.integratedcircuit.client;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Arrays;
import java.util.List;

public class IntegratedCircuitBlockTint {

    public static List<BlockTintSource> createBlockTintSources() {
        return Arrays.stream(FlatDirection.VALUES).map(IntegratedCircuitBlockTint::createBlockTintSource).toList();
    }

    private static BlockTintSource createBlockTintSource(FlatDirection circuitDirection) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return RedstoneWireBlock.getColorForPower(0);
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return RedstoneWireBlock.getColorForPower(getPower(level, pos));
            }

            public int getPower(BlockAndTintGetter level, BlockPos pos) {
                if(level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                    return integratedCircuitBlockEntity.getPortRenderStrength(circuitDirection);
                }
                return 0;
            }
        };
    }
}
