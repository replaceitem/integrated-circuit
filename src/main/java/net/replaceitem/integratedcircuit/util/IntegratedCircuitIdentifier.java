package net.replaceitem.integratedcircuit.util;

import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

public class IntegratedCircuitIdentifier extends Identifier {
    public IntegratedCircuitIdentifier(String path) {
        super(IntegratedCircuit.MOD_ID, path);
    }
}
