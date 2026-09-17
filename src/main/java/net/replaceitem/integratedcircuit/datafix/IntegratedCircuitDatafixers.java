package net.replaceitem.integratedcircuit.datafix;

import com.mojang.datafixers.DSL;

public class IntegratedCircuitDatafixers {
    public static class References {
        public static final DSL.TypeReference COMPONENT_STATE = reference("component_state");
        public static final DSL.TypeReference CIRCUIT = reference("circuit");

        private static DSL.TypeReference reference(String name) {
            return net.minecraft.util.datafix.fixes.References.reference("integrated_circuit:" + name);
        }
    }
}
