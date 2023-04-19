package net.replaceitem.integratedcircuit.circuit;

import net.replaceitem.integratedcircuit.circuit.components.*;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;

import java.util.HashMap;
import java.util.Map;

public class Components {

    private static final Map<Integer, Component> COMPONENTS = new HashMap<>();


    public static final AirComponent AIR = register(new AirComponent(0));
    public static final BlockComponent BLOCK = register(new BlockComponent(1));
    public static final WireComponent WIRE = register(new WireComponent(2));
    public static final TorchComponent TORCH = register(new TorchComponent(3));
    public static final RepeaterComponent REPEATER = register(new RepeaterComponent(4));
    public static final ComparatorComponent COMPARATOR = register(new ComparatorComponent(5));
    public static final ObserverComponent OBSERVER = register(new ObserverComponent(6));
    public static final TargetComponent TARGET = register(new TargetComponent(7));
    public static final RedstoneBlockComponent REDSTONE_BLOCK = register(new RedstoneBlockComponent(8));
    public static final PortComponent PORT = register(new PortComponent(9));
    public static final CrossoverComponent CROSSOVER = register(new CrossoverComponent(10));
    public static final LeverComponent LEVER = register(new LeverComponent(11));
    public static final ButtonComponent STONE_BUTTON = register(new ButtonComponent(12, false));
    public static final ButtonComponent WOODEN_BUTTON = register(new ButtonComponent(13, true));


    public static final ComponentState AIR_DEFAULT_STATE = AIR.getDefaultState();

    private static <C extends Component> C register(C component) {
        COMPONENTS.put(component.getId(), component);
        return component;
    }
    
    public static Component getComponentById(int id) {
        Component component = COMPONENTS.get(id);
        if(component == null) throw new IllegalArgumentException("Invalid component id: " + id);
        return component;
    }

    public static ComponentState createComponentState(short data) {
        return getComponentById(data & 0xFF).getState((byte) ((data >> 8) & 0xFF));
    }
}
