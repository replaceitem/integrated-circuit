package net.replaceitem.integratedcircuit.circuit;

import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;


/**
 * Replicates {@link net.minecraft.world.block.SimpleNeighborUpdater} but for circuits
 */

public class CircuitNeighborUpdater {
    
    public static final FlatDirection[] UPDATE_ORDER = new FlatDirection[]{FlatDirection.WEST, FlatDirection.EAST, FlatDirection.NORTH, FlatDirection.SOUTH};
    
    protected final ServerCircuit circuit;
    
    public CircuitNeighborUpdater(ServerCircuit circuit) {
        this.circuit = circuit;
    }

    public void replaceWithStateForNeighborUpdate(FlatDirection direction, ComponentState neighborState, ComponentPos pos, ComponentPos neighborPos, int flags) {
        CircuitNeighborUpdater.replaceWithStateForNeighborUpdate(this.circuit, direction, neighborState, pos, neighborPos, flags);
    }
    
    public static void replaceWithStateForNeighborUpdate(ServerCircuit world, FlatDirection direction, ComponentState neighborState, ComponentPos pos, ComponentPos neighborPos, int flags) {
        ComponentState blockState = world.getComponentState(pos);
        ComponentState blockState2 = blockState.getStateForNeighborUpdate(direction, neighborState, world, pos, neighborPos);
        Component.replace(blockState, blockState2, world, pos, flags);
    }

    public void updateNeighbor(ComponentPos pos, Component sourceBlock, ComponentPos sourcePos) {
        ComponentState blockState = this.circuit.getComponentState(pos);
        this.updateNeighbor(blockState, pos, sourceBlock, sourcePos, false);
    }

    public void updateNeighbor(ComponentState state, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        CircuitNeighborUpdater.tryNeighborUpdate(this.circuit, state, pos, sourceBlock, sourcePos, notify);
    }

    public void updateNeighbors(ComponentPos pos, Component sourceBlock, @Nullable FlatDirection except) {
        for (FlatDirection direction : UPDATE_ORDER) {
            if (direction == except) continue;
            this.updateNeighbor(pos.offset(direction), sourceBlock, pos);
        }
    }

    public static void tryNeighborUpdate(ServerCircuit world, ComponentState state, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        state.neighborUpdate(world, pos, sourceBlock, sourcePos, notify);
    }
}
