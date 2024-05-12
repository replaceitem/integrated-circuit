package net.replaceitem.integratedcircuit.circuit;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.replaceitem.integratedcircuit.circuit.context.CircuitContext;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.ContextCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;

public class CircuitTickScheduler {
    
    public static final ContextCodec<CircuitContext, CircuitTickScheduler> CODEC = context -> OrderedCircuitTick.CODEC.withContext(context)
            .listOf()
            .xmap(CircuitTickScheduler::new, circuitTickScheduler -> circuitTickScheduler.getTickQueue().stream().toList());
    
    private static final CircuitTickScheduler EMPTY_SCHEDULER = new CircuitTickScheduler() {
        @Override
        public void scheduleTick(OrderedCircuitTick orderedTick) {}

        @Override
        public boolean isQueued(ComponentPos pos, Component component) {
            return false;
        }

        @Override
        public boolean isTicking(ComponentPos pos, Component component) {
            return false;
        }
    };

    public static CircuitTickScheduler getClientTickScheduler() {
        return EMPTY_SCHEDULER;
    }


    private final Set<OrderedCircuitTick> queuedTicks = new ObjectOpenCustomHashSet<>(OrderedCircuitTick.HASH_STRATEGY);

    public Queue<OrderedCircuitTick> getTickQueue() {
        return tickQueue;
    }

    private final Queue<OrderedCircuitTick> tickQueue = new PriorityQueue<>(OrderedCircuitTick.TRIGGER_TICK_COMPARATOR);
    
    public CircuitTickScheduler() {
        
    }
    
    private CircuitTickScheduler(Collection<OrderedCircuitTick> ticks) {
        ticks.forEach(this::queueTick);
    }
    
    public void scheduleTick(OrderedCircuitTick orderedTick) {
        if (this.queuedTicks.add(orderedTick)) {
            this.queueTick(orderedTick);
        }
    }

    public void queueTick(OrderedCircuitTick orderedTick) {
        this.tickQueue.add(orderedTick);
    }

    @Nullable
    public OrderedCircuitTick peekNextTick() {
        return this.tickQueue.peek();
    }

    @Nullable
    public OrderedCircuitTick pollNextTick() {
        OrderedCircuitTick orderedTick = this.tickQueue.poll();
        if (orderedTick != null) {
            this.queuedTicks.remove(orderedTick);
        }
        return orderedTick;
    }

    public void tick(long time, int maxTicks, BiConsumer<ComponentPos, Component> ticker) {
        OrderedCircuitTick orderedCircuitTick;
        while(((orderedCircuitTick = peekNextTick()) != null) && orderedCircuitTick.triggerTick() <= time) {
            pollNextTick();
            ticker.accept(orderedCircuitTick.pos(), orderedCircuitTick.type());
        }
    }

    public boolean isTicking(ComponentPos pos, Component component) {
        return this.queuedTicks.contains(OrderedCircuitTick.create(component, pos));
    }

    public boolean isQueued(ComponentPos pos, Component component) {
        return this.queuedTicks.contains(OrderedCircuitTick.create(component, pos));
    }
}
