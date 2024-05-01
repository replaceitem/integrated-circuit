package net.replaceitem.integratedcircuit.circuit.datafix;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

public class BlockEntityFixer {
    public static void fix(NbtCompound nbt) {
        int dataVersion = IntegratedCircuit.getDataVersion(nbt, 0);
        if(dataVersion < 1) {
            NbtCompound circuitNbt;
            if(nbt.contains("circuit", NbtElement.COMPOUND_TYPE)) {
                circuitNbt = nbt.getCompound("circuit");
            } else {
                circuitNbt = new NbtCompound();
                nbt.put("circuit", circuitNbt);
            }
            moveKey(nbt, circuitNbt, "ports");
            moveKey(nbt, circuitNbt, "components");
            moveKey(nbt, circuitNbt, "tickScheduler");
        }
    }
    
    private static void moveKey(NbtCompound from, NbtCompound to, String key) {
        if(from.get(key) != null) {
            to.put(key, from.get(key));
            from.remove(key);
        }
    }
}
