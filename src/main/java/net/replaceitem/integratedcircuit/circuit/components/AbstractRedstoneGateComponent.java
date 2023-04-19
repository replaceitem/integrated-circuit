package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.*;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public abstract class AbstractRedstoneGateComponent extends FacingComponent {

    protected static final BooleanComponentProperty POWERED = new BooleanComponentProperty("powered", 2);

    public AbstractRedstoneGateComponent(int id, Text name) {
        super(id, name);
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if (this.isLocked(circuit, pos, state)) {
            return;
        }
        boolean powered = state.get(POWERED);
        boolean hasPower = this.hasPower(circuit, pos, state);
        if (powered && !hasPower) {
            circuit.setComponentState(pos, state.with(POWERED, false), Block.NOTIFY_LISTENERS);
        } else if (!powered) {
            circuit.setComponentState(pos, state.with(POWERED, true), Block.NOTIFY_LISTENERS);
            if (!hasPower) {
                circuit.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), TickPriority.VERY_HIGH);
            }
        }
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.getWeakRedstonePower(circuit, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (!state.get(POWERED)) {
            return 0;
        }
        if (state.get(FACING) == direction) {
            return this.getOutputLevel(circuit, pos, state);
        }
        return 0;
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (state.canPlaceAt(circuit, pos)) {
            this.updatePowered(circuit, pos, state);
            return;
        }
        circuit.removeBlock(pos);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    protected void updatePowered(Circuit circuit, ComponentPos pos, ComponentState state) {
        if (this.isLocked(circuit, pos, state)) {
            return;
        }
        boolean isPowered = state.get(POWERED);
        if (isPowered != this.hasPower(circuit, pos, state) && !circuit.getCircuitTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.isTargetNotAligned(circuit, pos, state)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (isPowered) {
                tickPriority = TickPriority.VERY_HIGH;
            }
            circuit.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), tickPriority);
        }
    }

    public boolean isLocked(Circuit circuit, ComponentPos pos, ComponentState state) {
        return false;
    }

    protected boolean hasPower(Circuit circuit, ComponentPos pos, ComponentState state) {
        return this.getPower(circuit, pos, state) > 0;
    }

    protected int getPower(Circuit circuit, ComponentPos pos, ComponentState state) {
        FlatDirection facing = state.get(FACING);
        ComponentPos blockPos = pos.offset(facing);
        int i = circuit.getEmittedRedstonePower(blockPos, facing);
        if (i >= 15) {
            return i;
        }
        ComponentState blockState = circuit.getComponentState(blockPos);
        return Math.max(i, (blockState.isOf(Components.WIRE)) ? blockState.get(WireComponent.POWER) : 0);
    }

    protected int getMaxInputLevelSides(Circuit circuit, ComponentPos pos, ComponentState state) {
        FlatDirection direction = state.get(FACING);
        FlatDirection direction2 = direction.rotated(1);
        FlatDirection direction3 = direction.rotated(-1);
        return Math.max(this.getInputLevel(circuit, pos.offset(direction2), direction2), this.getInputLevel(circuit, pos.offset(direction3), direction3));
    }

    protected int getInputLevel(Circuit circuit, ComponentPos pos, FlatDirection dir) {
        ComponentState state = circuit.getComponentState(pos);
        if (this.isValidInput(state)) {
            if (state.isOf(Components.REDSTONE_BLOCK)) {
                return 15;
            }
            if (state.isOf(Components.WIRE)) {
                return state.get(WireComponent.POWER);
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
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        return this.getDefaultState().with(FACING, rotation);
    }

    @Override
    public void onPlaced(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if (this.hasPower(circuit, pos, state)) {
            circuit.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        this.updateTarget(circuit, pos, state);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if (state.isOf(newState.getComponent())) {
            return;
        }
        super.onStateReplaced(state, circuit, pos, newState);
        this.updateTarget(circuit, pos, state);
    }

    protected void updateTarget(Circuit circuit, ComponentPos pos, ComponentState state) {
        FlatDirection direction = state.get(FACING);
        ComponentPos blockPos = pos.offset(direction.getOpposite());
        circuit.updateNeighbor(blockPos, this, pos);
        circuit.updateNeighborsExcept(blockPos, this, direction);
    }

    protected boolean isValidInput(ComponentState state) {
        return state.emitsRedstonePower();
    }

    protected int getOutputLevel(Circuit circuit, ComponentPos pos, ComponentState state) {
        return 15;
    }

    public static boolean isRedstoneGate(ComponentState state) {
        return state.getComponent() instanceof AbstractRedstoneGateComponent;
    }

    public boolean isTargetNotAligned(Circuit world, ComponentPos pos, ComponentState state) {
        FlatDirection direction = state.get(FACING).getOpposite();
        ComponentState blockState = world.getComponentState(pos.offset(direction));
        return AbstractRedstoneGateComponent.isRedstoneGate(blockState) && state.get(FACING) != direction;
    }

    protected abstract int getUpdateDelayInternal(ComponentState state);

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(POWERED);
    }
}
