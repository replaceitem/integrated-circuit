package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.components.*;

public class Components {
    public static final AirComponent AIR = register("air", new AirComponent(new Component.Settings()));
    public static final BlockComponent BLOCK = register("block", new BlockComponent(new Component.Settings().sounds(BlockSoundGroup.WOOL)));
    public static final WireComponent WIRE = register("wire", new WireComponent(new Component.Settings()));
    public static final TorchComponent TORCH = register("torch", new TorchComponent(new Component.Settings().sounds(BlockSoundGroup.WOOD)));
    public static final RepeaterComponent REPEATER = register("repeater", new RepeaterComponent(new Component.Settings().sounds(BlockSoundGroup.WOOD)));
    public static final ComparatorComponent COMPARATOR = register("comparator", new ComparatorComponent(new Component.Settings().sounds(BlockSoundGroup.WOOD)));
    public static final ObserverComponent OBSERVER = register("observer", new ObserverComponent(new Component.Settings()));
    public static final TargetComponent TARGET = register("target", new TargetComponent(new Component.Settings().sounds(BlockSoundGroup.GRASS)));
    public static final RedstoneBlockComponent REDSTONE_BLOCK = register("redstone_block", new RedstoneBlockComponent(new Component.Settings().sounds(BlockSoundGroup.METAL)));
    public static final PortComponent PORT = register("port", new PortComponent(new Component.Settings()));
    public static final CrossoverComponent CROSSOVER = register("crossover", new CrossoverComponent(new Component.Settings()));
    public static final LeverComponent LEVER = register("lever", new LeverComponent(new Component.Settings().sounds(BlockSoundGroup.WOOD)));
    public static final ButtonComponent STONE_BUTTON = register("stone_button", new ButtonComponent(new Component.Settings(), false));
    public static final ButtonComponent WOODEN_BUTTON = register("wooden_button", new ButtonComponent(new Component.Settings().sounds(BlockSoundGroup.WOOD), true));
    public static final LampComponent LAMP = register("lamp", new LampComponent(new Component.Settings().sounds(BlockSoundGroup.GLASS)));
    public static final LecternComponent LECTERN = register("lectern", new LecternComponent(new Component.Settings().sounds(BlockSoundGroup.WOOD)));
    public static final CopperBulbComponent COPPER_BULB = register("copper_bulb", new CopperBulbComponent(new Component.Settings().sounds(BlockSoundGroup.COPPER_BULB)));


    public static final ComponentState AIR_DEFAULT_STATE = AIR.getDefaultState();

    private static <T extends Component> T register(String id, T component) {
        return register(IntegratedCircuit.id(id), component);
    }
    
    public static <T extends Component> T register(Identifier id, T component) {
        return Registry.register(IntegratedCircuit.COMPONENTS_REGISTRY, id, component);
    }
    
    public static  <T extends Component> T register(RegistryKey<Component> key, T component) {
        return Registry.register(IntegratedCircuit.COMPONENTS_REGISTRY, key, component);
    }

    static {
        for(Component component : IntegratedCircuit.COMPONENTS_REGISTRY) {
            for(ComponentState componentState : component.getStateManager().getStates()) {
                Component.STATE_IDS.add(componentState);
            }
        }
    }

    public static void register() {
        // noop
    }
}
