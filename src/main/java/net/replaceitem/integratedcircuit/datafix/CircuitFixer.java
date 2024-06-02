package net.replaceitem.integratedcircuit.datafix;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSection;
import net.replaceitem.integratedcircuit.circuit.CircuitTickScheduler;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.OrderedCircuitTick;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.util.ComponentPos;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CircuitFixer {
    
    
    public static Optional<Dynamic<?>> fixPortStates(Dynamic<?> dynamic) {
        return dynamic.asListOpt(Dynamic::asNumber).result().map(list -> list.stream()
                    .map(DataResult::result)
                    .map(number -> number
                            .map(Number::byteValue)
                            .map(aByte -> {
                                ComponentState componentState = ComponentStateFixer.convertPort(aByte);
                                return ComponentState.CODEC.encodeStart(NbtOps.INSTANCE, componentState);
                            })
                            .flatMap(DataResult::result)
                            .map(nbtElement -> new Dynamic<>(NbtOps.INSTANCE, nbtElement))
                    )
        ).flatMap(optionalStream -> {
            List<Optional<Dynamic<NbtElement>>> list = optionalStream.toList();
            return list.stream().anyMatch(Optional::isEmpty) ? Optional.empty() : Optional.of(list.stream().map(Optional::orElseThrow));
        }).map(dynamic::createList);
        
    }

    public static CircuitTickScheduler readLegacyTickScheduler(NbtCompound nbt, ServerCircuitContext context) {
        if(!nbt.contains("tickScheduler", NbtElement.LIST_TYPE)) return new CircuitTickScheduler();
        NbtList tickSchedulerNbt = nbt.getList("tickScheduler", NbtElement.COMPOUND_TYPE);
        CircuitTickScheduler circuitTickScheduler = new CircuitTickScheduler();
        for (NbtElement nbtElement : tickSchedulerNbt) {
            if(!(nbtElement instanceof NbtCompound nbtCompound)) continue;
            OrderedCircuitTick orderedCircuitTick = readOrderedTick(nbtCompound, context);
            circuitTickScheduler.queueTick(orderedCircuitTick);
        }
        return circuitTickScheduler;
    }

    public static OrderedCircuitTick readOrderedTick(NbtCompound nbt, ServerCircuitContext context) {
        return new OrderedCircuitTick(
                ComponentStateFixer.convertComponent(nbt.getByte("c")),
                new ComponentPos(nbt.getInt("x"), nbt.getInt("y")),
                context.getTime() + nbt.getInt("t"),
                TickPriority.byIndex(nbt.getInt("p")),
                nbt.getLong("s")
        );
    }

    public static Optional<Dynamic<?>> fixCircuitSection(Dynamic<?> components) {
        return components.asIntStreamOpt().map(IntStream::toArray).result()
                .map(CircuitFixer::fixComponents)
                .map(circuitSection -> CircuitSection.CODEC.encodeStart(NbtOps.INSTANCE, circuitSection))
                .flatMap(DataResult::result)
                .map(nbtElement -> new Dynamic<>(NbtOps.INSTANCE, nbtElement));
    }

    public static Optional<Dynamic<?>> fixTickScheduler(Dynamic<?> dynamic) {
        return dynamic.asListOpt(Function.identity()).result()
                .map(dynamics -> dynamic.createList(dynamics.stream()
                        .map(CircuitFixer::fixOrderedTick)
                        .filter(Optional::isPresent)
                        .map(Optional::orElseThrow)
                ));
    }

    private static Optional<Dynamic<?>> fixOrderedTick(Dynamic<?> dynamic) {
        Optional<Dynamic<?>> optionalC = dynamic.get("c").asNumber().result()
                .map(number -> ComponentStateFixer.convertComponent(number.byteValue()))
                .map(IntegratedCircuit.COMPONENTS_REGISTRY::getId)
                .map(identifier -> dynamic.createString(identifier.toString()));
        return optionalC.map(value -> dynamic.set("c", value));
    }

    private static CircuitSection fixComponents(int[] components) {
        int componentDataSize = 15*15;
        if (components.length != MathHelper.ceilDiv(componentDataSize, 2)) return new CircuitSection();
        CircuitSection section = new CircuitSection();
        PalettedContainer<ComponentState> componentStateContainer = section.getComponentStateContainer();
        for (int i = 0; i < componentDataSize; i++) {
            int shift = (i % 2 == 0) ? 16 : 0;
            componentStateContainer.set(i%15, i/15, 0, ComponentStateFixer.convertToState((short) (components[i / 2] >> shift & 0xFFFF)));
        }
        return section;
    }
}
