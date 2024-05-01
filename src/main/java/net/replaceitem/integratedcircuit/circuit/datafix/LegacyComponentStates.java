package net.replaceitem.integratedcircuit.circuit.datafix;

import com.mojang.logging.LogUtils;
import net.minecraft.block.enums.ComparatorMode;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.components.ButtonComponent;
import net.replaceitem.integratedcircuit.circuit.components.ComparatorComponent;
import net.replaceitem.integratedcircuit.circuit.components.CrossoverComponent;
import net.replaceitem.integratedcircuit.circuit.components.FacingComponent;
import net.replaceitem.integratedcircuit.circuit.components.LampComponent;
import net.replaceitem.integratedcircuit.circuit.components.LeverComponent;
import net.replaceitem.integratedcircuit.circuit.components.ObserverComponent;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.circuit.components.RepeaterComponent;
import net.replaceitem.integratedcircuit.circuit.components.TorchComponent;
import net.replaceitem.integratedcircuit.circuit.components.WireComponent;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.slf4j.Logger;

import static net.replaceitem.integratedcircuit.circuit.Components.*;

public class LegacyComponentStates {

    public static final Logger LOGGER = LogUtils.getLogger();

    private record Mapping(Component component, Converter stateConverter) {
        public Mapping(Component component) {
            this(component, (state, data) -> state);
        }

        ComponentState readState(byte data) {
            return stateConverter.convert(this.component.getDefaultState(), data);
        };
    }
    private interface Converter {
        ComponentState convert(ComponentState state, byte data);
    }

    private static final Mapping[] MAPPINGS = {
            new Mapping(AIR),
            new Mapping(BLOCK),
            new Mapping(WIRE, LegacyComponentStates::convertWireState),
            new Mapping(TORCH, LegacyComponentStates::convertTorchState),
            new Mapping(REPEATER, LegacyComponentStates::convertRepeaterState),
            new Mapping(COMPARATOR, LegacyComponentStates::convertComparatorState),
            new Mapping(OBSERVER, LegacyComponentStates::convertObserverState),
            new Mapping(TARGET),
            new Mapping(REDSTONE_BLOCK),
            new Mapping(PORT, LegacyComponentStates::convertPortState),
            new Mapping(CROSSOVER, LegacyComponentStates::convertCrossoverState),
            new Mapping(LEVER, LegacyComponentStates::convertLeverState),
            new Mapping(STONE_BUTTON, LegacyComponentStates::convertButtonState),
            new Mapping(WOODEN_BUTTON, LegacyComponentStates::convertButtonState),
            new Mapping(LAMP, LegacyComponentStates::convertLampState)
    };
    
    public static ComponentState convertPort(byte portData) {
        return MAPPINGS[9].readState(portData);
    }
    
    public static Component convertComponent(byte id) {
        id = id < 0 || id >= MAPPINGS.length ? 0 : id;
        return MAPPINGS[id].component();
    }
    
    public static ComponentState convertToState(short state) {
        byte componentId = (byte) (state & 0xFF);
        byte stateData = (byte) ((state >> 8) & 0xFF);
        try {
            if(componentId < 0 || componentId >= MAPPINGS.length) throw new IllegalArgumentException("Invalid component id " + componentId);;
            Mapping converter = MAPPINGS[componentId];
            return converter.readState(stateData);
        } catch (RuntimeException e) {
            LOGGER.error("Could not datafix state {}", state, e);
            return AIR_DEFAULT_STATE;
        }
    }

    private static ComponentState convertWireState(ComponentState state, byte stateData) {
        return state
                .with(WireComponent.CONNECTED_NORTH, getBit(stateData, 0))
                .with(WireComponent.CONNECTED_EAST, getBit(stateData, 1))
                .with(WireComponent.CONNECTED_SOUTH, getBit(stateData, 2))
                .with(WireComponent.CONNECTED_WEST, getBit(stateData, 3))
                .with(WireComponent.POWER, getBits(stateData, 4, 4));
    }

    private static ComponentState convertTorchState(ComponentState state, byte stateData) {
        return withFacing(state, stateData)
                .with(TorchComponent.LIT, getBit(stateData, 2));
    }

    private static ComponentState convertRepeaterState(ComponentState state, byte stateData) {
        return withFacing(state, stateData)
                .with(RepeaterComponent.POWERED, getBit(stateData, 2))
                .with(RepeaterComponent.DELAY, 1 + getBits(stateData, 3, 2))
                .with(RepeaterComponent.LOCKED, getBit(stateData, 5));
    }

    private static ComponentState convertComparatorState(ComponentState state, byte stateData) {
        return withFacing(state, stateData)
                .with(ComparatorComponent.POWERED, getBit(stateData, 2))
                .with(ComparatorComponent.MODE, getBit(stateData, 3) ? ComparatorMode.SUBTRACT : ComparatorMode.COMPARE)
                .with(ComparatorComponent.OUTPUT_POWER, getBits(stateData, 4, 4));
    }

    private static ComponentState convertObserverState(ComponentState state, byte stateData) {
        return withFacing(state, stateData)
                .with(ObserverComponent.POWERED, getBit(stateData, 3));
    }

    private static ComponentState convertPortState(ComponentState state, byte stateData) {
        return state
                .with(PortComponent.FACING, DIRECTIONS[getBits(stateData, 0, 2)])
                .with(PortComponent.POWER, getBits(stateData, 3, 4))
                .with(PortComponent.IS_OUTPUT, getBit(stateData, 7));
    }

    private static ComponentState convertCrossoverState(ComponentState state, byte stateData) {
        return state
                .with(CrossoverComponent.POWER_X, getBits(stateData, 0, 4))
                .with(CrossoverComponent.POWER_Y, getBits(stateData, 4, 4));
    }

    private static ComponentState convertLeverState(ComponentState state, byte stateData) {
        return withFacing(state, stateData)
                .with(LeverComponent.POWERED, getBit(stateData, 3));
    }

    private static ComponentState convertButtonState(ComponentState state, byte stateData) {
        return withFacing(state, stateData)
                .with(ButtonComponent.POWERED, getBit(stateData, 3));
    }

    private static ComponentState convertLampState(ComponentState state, byte stateData) {
        return state
                .with(LampComponent.LIT, getBit(stateData, 0));
    }

    private static final FlatDirection[] DIRECTIONS = {FlatDirection.NORTH, FlatDirection.EAST, FlatDirection.SOUTH, FlatDirection.WEST};
    
    private static ComponentState withFacing(ComponentState state, byte stateData) {
        return state.with(FacingComponent.FACING, DIRECTIONS[getBits(stateData, 0, 2)]);
    }
    
    private static boolean getBit(byte value, int pos) {
        return ((value >> pos) & 1) != 0;
    }
    
    private static int getBits(byte value, int pos, int size) {
        return (value >> pos) & ((1 << size) - 1);
    }
}
