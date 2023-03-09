package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.AbstractRedstoneGateComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.RotatableComponentState;
import net.replaceitem.integratedcircuit.circuit.state.WireComponentState;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public abstract class AbstractRedstoneGateComponent extends Component {


    public AbstractRedstoneGateComponent(int id, Text name) {
        super(id, name);
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if(!(state instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (this.isLocked(circuit, pos, state)) {
            return;
        }
        boolean powered = abstractRedstoneGateComponentState.isPowered();
        boolean hasPower = this.hasPower(circuit, pos, abstractRedstoneGateComponentState);
        if (powered && !hasPower) {
            circuit.setComponentState(pos, ((AbstractRedstoneGateComponentState) state.copy()).setPowered(false), Block.NOTIFY_LISTENERS);
        } else if (!powered) {
            circuit.setComponentState(pos, ((AbstractRedstoneGateComponentState) state.copy()).setPowered(true), Block.NOTIFY_LISTENERS);
            if (!hasPower) {
                circuit.createAndScheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), TickPriority.VERY_HIGH);
            }
        }
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, ServerCircuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.getWeakRedstonePower(circuit, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, ServerCircuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (!abstractRedstoneGateComponentState.isPowered()) {
            return 0;
        }
        if (abstractRedstoneGateComponentState.getRotation() == direction) {
            return this.getOutputLevel(circuit, pos, state);
        }
        return 0;
    }

    @Override
    public void neighborUpdate(ComponentState state, ServerCircuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if(!(state instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (state.canPlaceAt(circuit, pos)) {
            this.updatePowered(circuit, pos, abstractRedstoneGateComponentState);
            return;
        }
        circuit.breakBlock(pos);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    protected void updatePowered(ServerCircuit world, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        if (this.isLocked(world, pos, state)) {
            return;
        }
        boolean isPowered = state.isPowered();
        if (isPowered != this.hasPower(world, pos, state) && !world.getCircuitTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.isTargetNotAligned(world, pos, state)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (isPowered) {
                tickPriority = TickPriority.VERY_HIGH;
            }
            world.createAndScheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), tickPriority);
        }
    }

    public boolean isLocked(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        return false;
    }

    protected boolean hasPower(ServerCircuit circuit, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        return this.getPower(circuit, pos, state) > 0;
    }

    protected int getPower(ServerCircuit circuit, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        FlatDirection direction = state.getRotation();
        ComponentPos blockPos = pos.offset(direction);
        int i = circuit.getEmittedRedstonePower(blockPos, direction);
        if (i >= 15) {
            return i;
        }
        ComponentState blockState = circuit.getComponentState(blockPos);
        return Math.max(i, (blockState.isOf(Components.WIRE) && blockState instanceof WireComponentState wireComponentState) ? wireComponentState.getPower() : 0);
    }

    protected int getMaxInputLevelSides(ServerCircuit circuit, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        FlatDirection direction = state.getRotation();
        FlatDirection direction2 = direction.rotated(1);
        FlatDirection direction3 = direction.rotated(-1);
        return Math.max(this.getInputLevel(circuit, pos.offset(direction2), direction2), this.getInputLevel(circuit, pos.offset(direction3), direction3));
    }

    protected int getInputLevel(ServerCircuit circuit, ComponentPos pos, FlatDirection dir) {
        ComponentState state = circuit.getComponentState(pos);
        if (this.isValidInput(state)) {
            if (state.isOf(Components.REDSTONE_BLOCK)) {
                return 15;
            }
            if (state.isOf(Components.WIRE) && state instanceof WireComponentState wireComponentState) {
                return wireComponentState.getPower();
            }
            return circuit.getStrongRedstonePower(pos, dir);
        }
        return 0;
    }
    
    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public ComponentState getPlacementState(ServerCircuit circuit, ComponentPos pos, FlatDirection rotation) {
        return ((RotatableComponentState) this.getDefaultState()).setRotation(rotation);
    }

    @Override
    public void onPlaced(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (this.hasPower(circuit, pos, abstractRedstoneGateComponentState)) {
            circuit.createAndScheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void onBlockAdded(ComponentState state, ServerCircuit circuit, ComponentPos pos, ComponentState oldState) {
        if(!(state instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState)) throw new IllegalStateException("Invalid component state for component");
        this.updateTarget(circuit, pos, abstractRedstoneGateComponentState);
    }

    @Override
    public void onStateReplaced(ComponentState state, ServerCircuit circuit, ComponentPos pos, ComponentState newState) {
        if(!(state instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (state.isOf(newState.getComponent())) {
            return;
        }
        super.onStateReplaced(state, circuit, pos, newState);
        this.updateTarget(circuit, pos, abstractRedstoneGateComponentState);
    }

    protected void updateTarget(ServerCircuit circuit, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        FlatDirection direction = state.getRotation();
        ComponentPos blockPos = pos.offset(direction.getOpposite());
        circuit.updateNeighbor(blockPos, this, pos);
        circuit.updateNeighborsExcept(blockPos, this, direction);
    }

    protected boolean isValidInput(ComponentState state) {
        return state.emitsRedstonePower();
    }

    protected int getOutputLevel(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        return 15;
    }

    public static boolean isRedstoneGate(ComponentState state) {
        return state.getComponent() instanceof AbstractRedstoneGateComponent;
    }

    public boolean isTargetNotAligned(ServerCircuit world, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        FlatDirection direction = state.getRotation().getOpposite();
        ComponentState blockState = world.getComponentState(pos.offset(direction));
        return AbstractRedstoneGateComponent.isRedstoneGate(blockState) && blockState instanceof AbstractRedstoneGateComponentState abstractRedstoneGateComponentState && abstractRedstoneGateComponentState.getRotation() != direction;
    }

    protected abstract int getUpdateDelayInternal(ComponentState state);

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
