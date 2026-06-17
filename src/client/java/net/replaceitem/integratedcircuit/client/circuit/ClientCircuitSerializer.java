package net.replaceitem.integratedcircuit.client.circuit;

import net.minecraft.nbt.CompoundTag;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.client.circuit.context.ClientCircuitContext;

public class ClientCircuitSerializer extends CircuitSerializer {
    public ClientCircuitSerializer(CompoundTag nbt) {
        super(nbt);
    }

    public ClientCircuit readClientCircuit(ClientCircuitContext context) {
        return new ClientCircuit(
                context,
                readPortStates(),
                readSection()
        );
    }
}
