package net.replaceitem.integratedcircuit.circuit;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;

public class CircuitTickScheduler {

    private final Set<OrderedCircuitTick> queuedTicks = new ObjectOpenCustomHashSet<>(OrderedCircuitTick.HASH_STRATEGY);
    private final Queue<OrderedCircuitTick> tickQueue = new PriorityQueue<>(OrderedCircuitTick.TRIGGER_TICK_COMPARATOR);
    
    public CircuitTickScheduler() {
        
    }
    
    public void scheduleTick(OrderedCircuitTick orderedTick) {
        if (this.queuedTicks.add(orderedTick)) {
            this.queueTick(orderedTick);
        }
    }

    private void queueTick(OrderedCircuitTick orderedTick) {
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

    public void loadFromNbt(NbtList tickQueue, long time) {
        for (NbtElement nbtElement : tickQueue) {
            if(!(nbtElement instanceof NbtCompound nbtCompound)) continue;
            OrderedCircuitTick orderedCircuitTick = OrderedCircuitTick.fromNbt(nbtCompound, time);
            this.queueTick(orderedCircuitTick);
        }
    }
    
    public NbtList toNbt(long time) {
        NbtList nbtList = new NbtList();
        for (OrderedCircuitTick orderedTick : this.tickQueue) {
            nbtList.add(orderedTick.toNbt(time));
        }
        return nbtList;
    }
}
