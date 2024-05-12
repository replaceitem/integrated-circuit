package net.replaceitem.integratedcircuit.circuit;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ClientCircuitContext;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.circuit.datafix.LegacyCircuitDeserializer;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.slf4j.Logger;

import java.util.Arrays;

/*
NBT Structure in BlockEntity:

CustomName
outputStrengths
circuit
    ports
    tickScheduler
    section
        component_states
            (paletted container)

 */
public class CircuitSerializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String SECTION_TAG = "section";
    public static final String COMPONENT_STATES_TAG = "component_states";
    public static final String PORTS_TAG = "ports";
    public static final String TICK_SCHEDULER_TAG = "tickScheduler";
    
    private final int dataVersion;
    private final NbtCompound root;

    public CircuitSerializer(NbtCompound nbt) {
        this.dataVersion = IntegratedCircuit.getDataVersion(nbt, 0);
        this.root = nbt;
    }

    public ServerCircuit readServerCircuit(ServerCircuitContext context) {
        if(dataVersion < 1) return LegacyCircuitDeserializer.readLegacyServerCircuit(context, root);
        return ServerCircuit.CODEC.parse(context, NbtOps.INSTANCE, root).result().orElseGet(() -> new ServerCircuit(context));
    }

    public ClientCircuit readClientCircuit(ClientCircuitContext context) {
        return new ClientCircuit(
                context,
                readPortStates(),
                readSection()
        );
    }
    
    private CircuitTickScheduler readTickScheduler(ServerCircuitContext context) {
        if(dataVersion < 1) return LegacyCircuitDeserializer.readLegacyTickScheduler(root, context);
        if(!root.contains(TICK_SCHEDULER_TAG, NbtElement.LIST_TYPE)) return new CircuitTickScheduler();
        NbtList tickSchedulerNbt = root.getList(TICK_SCHEDULER_TAG, NbtElement.COMPOUND_TYPE);
        CircuitTickScheduler circuitTickScheduler = new CircuitTickScheduler();
        for (NbtElement nbtElement : tickSchedulerNbt) {
            if(!(nbtElement instanceof NbtCompound nbtCompound)) continue;
            OrderedCircuitTick orderedCircuitTick = readOrderedTick(nbtCompound, context);
            circuitTickScheduler.queueTick(orderedCircuitTick);
        }
        return circuitTickScheduler;
    }


    public static OrderedCircuitTick readOrderedTick(NbtCompound nbt, ServerCircuitContext context) {
        // todo more robust handling of missing values
        return new OrderedCircuitTick(
                IntegratedCircuit.COMPONENTS_REGISTRY.getOrEmpty(Identifier.tryParse(nbt.getString("c"))).orElseThrow(),
                new ComponentPos(nbt.getInt("x"), nbt.getInt("y")),
                context.getTime() + nbt.getInt("t"),
                TickPriority.byIndex(nbt.getInt("p")),
                nbt.getLong("s")
        );
    }

    private ComponentState[] readPortStates() {
        if(dataVersion < 1) return LegacyCircuitDeserializer.readLegacyPortStates(root);
        if(!root.contains(PORTS_TAG)) return Circuit.createDefaultPorts();
        NbtList ports = root.getList(PORTS_TAG, NbtElement.COMPOUND_TYPE);
        if(ports.size() != 4) return Circuit.createDefaultPorts();
        return ports.stream().map(nbtElement -> ComponentState.CODEC.parse(NbtOps.INSTANCE, nbtElement).result().orElse(Components.AIR_DEFAULT_STATE)).toArray(ComponentState[]::new);
    }

    public CircuitSection readSection() {
        if(dataVersion < 1) return LegacyCircuitDeserializer.readLegacySection(root);
        if(!root.contains(SECTION_TAG, NbtElement.COMPOUND_TYPE)) return new CircuitSection();
        NbtCompound sectionNbt = root.getCompound(SECTION_TAG);
        if(!sectionNbt.contains(COMPONENT_STATES_TAG, NbtElement.COMPOUND_TYPE)) return new CircuitSection();
        PalettedContainer<ComponentState> palettedContainer;
        palettedContainer = CircuitSection.PALETTE_CODEC.parse(NbtOps.INSTANCE, sectionNbt.getCompound(COMPONENT_STATES_TAG))
                .promotePartial(errorMessage -> CircuitSerializer.LOGGER.error("Could not load circuit: {}", errorMessage))
                .result()
                .orElseGet(CircuitSection::createContainer);
        return new CircuitSection(palettedContainer);
    }

    public static DataResult<NbtElement> writeCircuit(ServerCircuit circuit) {
        return ServerCircuit.CODEC.encodeStart(circuit.getContext(), NbtOps.INSTANCE, circuit)
                .ifSuccess(nbtElement -> {
                    if(nbtElement instanceof NbtCompound compound) IntegratedCircuit.putDataVersion(compound);
                });
    }

    private static NbtElement writePortStates(ComponentState[] portStates) {
        return NbtOps.INSTANCE.createList(
                Arrays.stream(portStates)
                        .map(componentState -> ComponentState.CODEC.encodeStart(NbtOps.INSTANCE, componentState))
                        .map(DataResult::getOrThrow)
        );
    }

    private static NbtList writeTickScheduler(CircuitTickScheduler tickScheduler, long time) {
        NbtList nbtList = new NbtList();
        for (OrderedCircuitTick orderedTick : tickScheduler.getTickQueue()) {
            nbtList.add(writeOrderedTick(orderedTick, time));
        }
        return nbtList;
    }

    public static NbtCompound writeOrderedTick(OrderedCircuitTick tick, long time) {
        NbtCompound nbtCompound = new NbtCompound();
        String id = IntegratedCircuit.COMPONENTS_REGISTRY.getId(tick.type()).toString();
        nbtCompound.putString("c", id);
        nbtCompound.putInt("x", tick.pos().getX());
        nbtCompound.putInt("y", tick.pos().getY());
        nbtCompound.putInt("t", (int) (tick.triggerTick() - time));
        nbtCompound.putInt("p", tick.priority().getIndex());
        nbtCompound.putLong("s", tick.subTickOrder());
        return nbtCompound;
    }

    public static NbtCompound writeSection(CircuitSection section) {
        NbtCompound nbt = new NbtCompound();
        nbt.put(COMPONENT_STATES_TAG, CircuitSection.PALETTE_CODEC.encodeStart(NbtOps.INSTANCE, section.getComponentStateContainer()).getOrThrow());
        return nbt;
    }
}
