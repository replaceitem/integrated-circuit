package net.replaceitem.integratedcircuit.circuit.context;

import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public interface ServerCircuitContext extends CircuitContext {
    Random getRandom();
    boolean isReady();
    void onComponentUpdate(ComponentPos pos, ComponentState state);
}
