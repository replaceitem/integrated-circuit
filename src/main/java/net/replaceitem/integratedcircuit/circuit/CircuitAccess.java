package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

/**
 * @see net.minecraft.world.WorldAccess
 */
public interface CircuitAccess {

    CircuitTickScheduler getCircuitTickScheduler();

    long getTime();
    long getTickOrder();

    default void scheduleBlockTick(ComponentPos pos, Component component, int delay, TickPriority priority) {
        this.getCircuitTickScheduler().scheduleTick(this.createOrderedTick(pos, component, delay, priority));
    }

    default void scheduleBlockTick(ComponentPos pos, Component component, int delay) {
        this.getCircuitTickScheduler().scheduleTick(this.createOrderedTick(pos, component, delay));
    }


    private OrderedCircuitTick createOrderedTick(ComponentPos pos, Component type, int delay, TickPriority priority) {
        return new OrderedCircuitTick(type, pos, this.getTime() + (long)delay, priority, this.getTickOrder());
    }

    private OrderedCircuitTick createOrderedTick(ComponentPos pos, Component type, int delay) {
        return new OrderedCircuitTick(type, pos, this.getTime() + (long)delay, this.getTickOrder());
    }



    default void updateNeighbors(ComponentPos pos, Component component) {}
    default void updateNeighborsExcept(ComponentPos pos, Component sourceComponent, FlatDirection direction) {}
    default void updateNeighborsAlways(ComponentPos pos, Component sourceComponent) {}
    default void updateNeighbor(ComponentPos pos, Component sourceComponent, ComponentPos sourcePos) {}
    default void updateNeighbor(ComponentState state, ComponentPos pos, Component sourceComponent, ComponentPos sourcePos, boolean notify) {}
    default void replaceWithStateForNeighborUpdate(FlatDirection direction, ComponentState neighborState, ComponentPos pos, ComponentPos neighborPos, int flags, int maxUpdateDepth) {}
}
