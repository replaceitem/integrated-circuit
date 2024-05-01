package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.world.chunk.PalettedContainer;

public abstract class FlatPaletteProvider extends PalettedContainer.PaletteProvider {
    
    int edgeSize;
    
    public FlatPaletteProvider(int edgeSize) {
        super(0);
        this.edgeSize = edgeSize;
    }

    @Override
    public int getContainerSize() {
        return edgeSize * edgeSize;
    }

    @Override
    public int computeIndex(int x, int y, int z) {
        return y * edgeSize + x;
    }
}
