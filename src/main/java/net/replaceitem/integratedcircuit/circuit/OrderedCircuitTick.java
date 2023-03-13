package net.replaceitem.integratedcircuit.circuit;


import it.unimi.dsi.fastutil.Hash;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public record OrderedCircuitTick(Component type, ComponentPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    public static final Comparator<OrderedCircuitTick> TRIGGER_TICK_COMPARATOR = Comparator.comparingLong((OrderedCircuitTick orderedCircuitTick) -> orderedCircuitTick.triggerTick).thenComparing(orderedCircuitTick -> orderedCircuitTick.priority).thenComparingLong(orderedCircuitTick -> orderedCircuitTick.subTickOrder);
    public static final Comparator<OrderedCircuitTick> BASIC_COMPARATOR = Comparator.comparing((OrderedCircuitTick orderedCircuitTick) -> orderedCircuitTick.priority).thenComparingLong(orderedCircuitTick -> orderedCircuitTick.subTickOrder);

    public static final Hash.Strategy<OrderedCircuitTick> HASH_STRATEGY = new Hash.Strategy<>() {
        public int hashCode(OrderedCircuitTick orderedTick) {
            return 31 * orderedTick.pos().hashCode() + orderedTick.type().hashCode();
        }

        public boolean equals(@Nullable OrderedCircuitTick orderedTick, @Nullable OrderedCircuitTick orderedTick2) {
            if (orderedTick == orderedTick2) {
                return true;
            } else if (orderedTick != null && orderedTick2 != null) {
                return orderedTick.type() == orderedTick2.type() && orderedTick.pos().equals(orderedTick2.pos());
            } else {
                return false;
            }
        }
    };
    
    public OrderedCircuitTick(Component type, ComponentPos pos, long triggerTick, long subTickOrder) {
        this(type, pos, triggerTick, TickPriority.NORMAL, subTickOrder);
    }
    
    public static OrderedCircuitTick create(Component type, ComponentPos pos) {
        return new OrderedCircuitTick(type, pos, 0L, TickPriority.NORMAL, 0L);
    }

    public static OrderedCircuitTick fromNbt(NbtCompound nbt, long time) {
        return new OrderedCircuitTick(
                Components.getComponentById(nbt.getByte("c")),
                new ComponentPos(nbt.getInt("x"), nbt.getInt("y")),
                time + nbt.getInt("t"),
                TickPriority.byIndex(nbt.getInt("p")),
                nbt.getLong("s")
        );
    }
    
    public NbtCompound toNbt(long time) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putByte("c", (byte) type.getId());
        nbtCompound.putInt("x", pos.getX());
        nbtCompound.putInt("y", pos.getY());
        nbtCompound.putInt("t", (int) (triggerTick - time));
        nbtCompound.putInt("p", priority.getIndex());
        nbtCompound.putLong("s", subTickOrder);
        return nbtCompound;
    }
}
