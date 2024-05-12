package net.replaceitem.integratedcircuit.circuit;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.context.CircuitContext;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.ContextCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public record OrderedCircuitTick(Component type, ComponentPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    
    public static final Codec<TickPriority> TICK_PRIORITY_CODEC = Codec.INT.xmap(TickPriority::byIndex, TickPriority::getIndex);
    
    public static final ContextCodec<CircuitContext, OrderedCircuitTick> CODEC = context -> RecordCodecBuilder.create(instance -> instance.group(
            IntegratedCircuit.COMPONENTS_REGISTRY.getCodec().fieldOf("c").forGetter(OrderedCircuitTick::type),
            ComponentPos.MAP_CODEC.forGetter(OrderedCircuitTick::pos),
            Codec.INT.fieldOf("t").xmap(integer -> context.getTime() + integer, l -> (int)(l - context.getTime())).forGetter(OrderedCircuitTick::triggerTick),
            TICK_PRIORITY_CODEC.fieldOf("p").forGetter(OrderedCircuitTick::priority),
            Codec.LONG.fieldOf("s").forGetter(OrderedCircuitTick::subTickOrder)
    ).apply(instance, OrderedCircuitTick::new));
    
    
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
}
