package net.replaceitem.integratedcircuit.circuit;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
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
        return root.getList(PORTS_TAG)
                .filter(nbtElements -> nbtElements.size() == 4)
                .map(ports -> ports.stream()
                        .map(nbtElement -> ComponentState.CODEC.parse(NbtOps.INSTANCE, nbtElement)
                                .result().orElse(Components.AIR_DEFAULT_STATE)
                        )
                        .toArray(ComponentState[]::new)
                )
                .orElseGet(Circuit::createDefaultPorts);
    }

    public CircuitSection readSection() {
        return root.getCompound(SECTION_TAG)
                .flatMap(sectionNbt -> sectionNbt.getCompound(COMPONENT_STATES_TAG))
                .flatMap(componentStatesNbt -> CircuitSection.PALETTE_CODEC.parse(NbtOps.INSTANCE, componentStatesNbt)
                        .promotePartial(errorMessage -> CircuitSerializer.LOGGER.error("Could not load circuit: {}", errorMessage))
                        .result()
                )
                .map(CircuitSection::new)
                .orElseGet(CircuitSection::new);
    }

    public static DataResult<NbtElement> writeCircuit(ServerCircuit circuit) {
        return ServerCircuit.CODEC.encodeStart(circuit.getContext(), NbtOps.INSTANCE, circuit);
    }
}
