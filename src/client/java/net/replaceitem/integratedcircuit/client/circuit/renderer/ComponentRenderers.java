package net.replaceitem.integratedcircuit.client.circuit.renderer;

import net.minecraft.util.Util;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.client.circuit.renderer.components.*;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ComponentRenderers {
    private static final Map<Component, ComponentRenderer> RENDERERS = Util.make(new HashMap<>(), map -> {
        map.put(Components.BLOCK, new FullComponentRenderer(IntegratedCircuit.id("textures/integrated_circuit/block.png")));
        map.put(Components.COPPER_BULB, new CopperBulbComponentRenderer());
        map.put(Components.WIRE, new WireComponentRenderer());
        map.put(Components.PORT, new PortComponentRenderer());
        map.put(Components.CROSSOVER, new CrossoverComponentRenderer());
        map.put(Components.WOODEN_BUTTON, new ButtonComponentRenderer(IntegratedCircuit.id("textures/integrated_circuit/button_wood.png")));
        map.put(Components.STONE_BUTTON, new ButtonComponentRenderer(IntegratedCircuit.id("textures/integrated_circuit/button_stone.png")));
        map.put(Components.OBSERVER, new ObserverComponentRenderer());
        map.put(Components.TORCH, new TorchComponentRenderer());
        map.put(Components.REPEATER, new RepeaterComponentRenderer());
        map.put(Components.COMPARATOR, new ComparatorComponentRenderer());
        map.put(Components.LEVER, new LeverComponentRenderer());
        map.put(Components.LAMP, new LampComponentRenderer());
        map.put(Components.LECTERN, new LecternComponentRenderer());
        map.put(Components.REDSTONE_BLOCK, new FullComponentRenderer(IntegratedCircuit.id("textures/integrated_circuit/redstone_block.png")));
        map.put(Components.TARGET, new FullComponentRenderer(IntegratedCircuit.id("textures/integrated_circuit/target.png")));
    });

    @Nullable
    public static ComponentRenderer get(Component component) {
        if(component == Components.AIR) return null;
        return RENDERERS.get(component);
    }
}
