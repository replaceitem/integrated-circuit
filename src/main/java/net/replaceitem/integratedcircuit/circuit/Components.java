package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.sound.BlockSoundGroup;
import net.replaceitem.integratedcircuit.circuit.components.*;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;

import java.util.HashMap;
import java.util.Map;

public class Components {

    private static final Map<Integer, Component> COMPONENTS = new HashMap<>();


    public static final AirComponent AIR = register(new AirComponent(0, new Component.Settings("air")));
    public static final BlockComponent BLOCK = register(new BlockComponent(1, new Component.Settings("block").sounds(BlockSoundGroup.WOOL)));
    public static final WireComponent WIRE = register(new WireComponent(2, new Component.Settings("wire")));
    public static final TorchComponent TORCH = register(new TorchComponent(3, new Component.Settings("torch").sounds(BlockSoundGroup.WOOD)));
    public static final RepeaterComponent REPEATER = register(new RepeaterComponent(4, new Component.Settings("repeater").sounds(BlockSoundGroup.WOOD)));
    public static final ComparatorComponent COMPARATOR = register(new ComparatorComponent(5, new Component.Settings("comparator").sounds(BlockSoundGroup.WOOD)));
    public static final ObserverComponent OBSERVER = register(new ObserverComponent(6, new Component.Settings("observer")));
    public static final TargetComponent TARGET = register(new TargetComponent(7, new Component.Settings("target").sounds(BlockSoundGroup.GRASS)));
    public static final RedstoneBlockComponent REDSTONE_BLOCK = register(new RedstoneBlockComponent(8, new Component.Settings("redstone_block").sounds(BlockSoundGroup.METAL)));
    public static final PortComponent PORT = register(new PortComponent(9, new Component.Settings("port")));
    public static final CrossoverComponent CROSSOVER = register(new CrossoverComponent(10, new Component.Settings("crossover")));
    public static final LeverComponent LEVER = register(new LeverComponent(11, new Component.Settings("lever").sounds(BlockSoundGroup.WOOD)));
    public static final ButtonComponent STONE_BUTTON = register(new ButtonComponent(12, new Component.Settings("stone_button"), false));
    public static final ButtonComponent WOODEN_BUTTON = register(new ButtonComponent(13, new Component.Settings("wooden_button").sounds(BlockSoundGroup.WOOD), true));


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
