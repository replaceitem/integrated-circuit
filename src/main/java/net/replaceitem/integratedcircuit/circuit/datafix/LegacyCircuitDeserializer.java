package net.replaceitem.integratedcircuit.circuit.datafix;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSection;
import net.replaceitem.integratedcircuit.circuit.CircuitTickScheduler;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.OrderedCircuitTick;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public class LegacyCircuitDeserializer {

    public static ServerCircuit readLegacyServerCircuit(ServerCircuitContext context, NbtCompound root) {
        return new ServerCircuit(
                context,
                readLegacyPortStates(root),
                readLegacySection(root),
                readLegacyTickScheduler(root, context)
        );
    }
    
    public static ComponentState[] readLegacyPortStates(NbtCompound root) {
        if(!root.contains("ports")) return Circuit.createDefaultPorts();
        byte[] ports = root.getByteArray("ports");
        if(ports.length != 4) return Circuit.createDefaultPorts();
        ComponentState[] portStates = new ComponentState[4];
        for (int i = 0; i < ports.length; i++) {
            byte port = ports[i];
            portStates[i] = LegacyComponentStates.convertPort(port);
        }
        return portStates;
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
                LegacyComponentStates.convertComponent(nbt.getByte("c")),
                new ComponentPos(nbt.getInt("x"), nbt.getInt("y")),
                context.getTime() + nbt.getInt("t"),
                TickPriority.byIndex(nbt.getInt("p")),
                nbt.getLong("s")
        );
    }

    public static CircuitSection readLegacySection(NbtCompound root) {
        if (!root.contains("components", NbtElement.INT_ARRAY_TYPE)) return new CircuitSection();
        int componentDataSize = 15*15;
        int[] componentData = root.getIntArray("components");

        if (componentData.length != MathHelper.ceilDiv(componentDataSize, 2)) return new CircuitSection();
        CircuitSection section = new CircuitSection();
        PalettedContainer<ComponentState> componentStateContainer = section.getComponentStateContainer();
        for (int i = 0; i < componentDataSize; i++) {
            int shift = (i % 2 == 0) ? 16 : 0;
            componentStateContainer.set(i%15, i/15, 0, LegacyComponentStates.convertToState((short) (componentData[i / 2] >> shift & 0xFFFF)));
        }
        return section;
    }
}
