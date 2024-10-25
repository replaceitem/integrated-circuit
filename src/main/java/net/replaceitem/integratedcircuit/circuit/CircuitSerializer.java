package net.replaceitem.integratedcircuit.circuit;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.chunk.PalettedContainer;
import net.replaceitem.integratedcircuit.circuit.context.ClientCircuitContext;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import org.slf4j.Logger;

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
    
    private final NbtCompound root;

    public CircuitSerializer(NbtCompound nbt) {
        this.root = nbt;
    }

    public ServerCircuit readServerCircuit(ServerCircuitContext context) {
        return ServerCircuit.CODEC.parse(context, NbtOps.INSTANCE, root).result().orElseGet(() -> new ServerCircuit(context));
    }

    public ClientCircuit readClientCircuit(ClientCircuitContext context) {
        return new ClientCircuit(
                context,
                readPortStates(),
                readSection()
        );
    }

    private ComponentState[] readPortStates() {
        if(!root.contains(PORTS_TAG)) return Circuit.createDefaultPorts();
        NbtList ports = root.getList(PORTS_TAG, NbtElement.COMPOUND_TYPE);
        if(ports.size() != 4) return Circuit.createDefaultPorts();
        return ports.stream().map(nbtElement -> ComponentState.CODEC.parse(NbtOps.INSTANCE, nbtElement).result().orElse(Components.AIR_DEFAULT_STATE)).toArray(ComponentState[]::new);
    }

    public CircuitSection readSection() {
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
        return ServerCircuit.CODEC.encodeStart(circuit.getContext(), NbtOps.INSTANCE, circuit);
    }
}
