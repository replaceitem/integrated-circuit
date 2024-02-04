package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class ServerCircuit extends Circuit<ServerCircuitContext> {
    protected final CircuitTickScheduler circuitTickScheduler = new CircuitTickScheduler();

    public ServerCircuit(ServerCircuitContext context) {
        super(false, context);
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return this.circuitTickScheduler;
    }

    @Override
    public void tick() {
        super.tick();
        this.circuitTickScheduler.tick(this.getTime(), 65536, this::tickBlock);
    }

    @Deprecated // TODO remove
    public void tick(World world, BlockPos pos, BlockState state, IntegratedCircuitBlockEntity blockEntity) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            int newPower = ((IntegratedCircuitBlock) state.getBlock()).getInputPower(world, pos, direction);
            Components.PORT.assignExternalPower(this, PORTS_GRID_POS[direction.toInt()], this.ports[direction.toInt()], newPower);
        }
        
        // tickScheduler used to be called here

        boolean updateNeeded = false;
        for (FlatDirection direction : FlatDirection.VALUES) {
            int oldPower = blockEntity.getOutputStrength(direction);
            int newPower = Components.PORT.getInternalPower(this, PORTS_GRID_POS[direction.toInt()], this.ports[direction.toInt()]);

            if(oldPower != newPower) {
                blockEntity.setOutputStrength(world, state, direction, newPower);
                updateNeeded = true;
            }
        }
        
        if(updateNeeded) {
            state.onBlockAdded(world, pos, state, true);
        }
        // Doing this as the end of each tick, since markDirty is not that cheap, since it makes comparator updates.
        // Having every block state change in the circuit trigger that would be a performance disadvantage.
        // TODO: remove the comparator update
        // this.blockEntity.markDirty();
    }

    private void tickBlock(ComponentPos pos, Component block) {
        ComponentState blockState = this.getComponentState(pos);
        if (blockState.isOf(block)) {
            blockState.scheduledTick(this, pos, this.context.getRandom());
        }
    }

    public static ServerCircuit fromNbt(NbtCompound nbt, ServerCircuitContext context) {
        if(nbt == null) return null;
        ServerCircuit circuit = new ServerCircuit(context);
        circuit.readNbt(nbt);
        return circuit;
    }

    private NbtList tickSchedulerNbtBuffer;

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtList tickSchedulerNbt = nbt.getList("tickScheduler", NbtElement.COMPOUND_TYPE);
        if(this.context.isReady()) {
            loadTickScheduler(tickSchedulerNbt);
        } else {
            // this has to be stored until the context is ready, since the time is needed to get the correct triggerTime for scheduled ticks
            this.tickSchedulerNbtBuffer = tickSchedulerNbt;
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("tickScheduler", this.circuitTickScheduler.toNbt(this.getTime()));
    }

    public void onWorldIsPresent() {
        if(this.tickSchedulerNbtBuffer != null) {
            loadTickScheduler(tickSchedulerNbtBuffer);
            this.tickSchedulerNbtBuffer = null;
        }
    }
    
    private void loadTickScheduler(NbtList list) {
        this.circuitTickScheduler.loadFromNbt(list, this.getTime());
    }

    @Override
    public void placeComponentState(ComponentPos pos, Component component, FlatDirection placementRotation) {
        ComponentState placementState = component.getPlacementState(this, pos, placementRotation);
        if(placementState == null) placementState = Components.AIR_DEFAULT_STATE;
        
        ComponentState beforeState = this.getComponentState(pos);
        if(beforeState.isAir() && placementState.isAir()) return;
        this.setComponentState(pos, placementState, Component.NOTIFY_ALL);
        placementState.getComponent().onPlaced(this, pos, placementState);
    }

    @Override
    protected void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags) {
        this.context.onComponentUpdate(pos, state);
    }

    @Override
    public void updateNeighbors(ComponentPos pos, Component component) {
        this.updateNeighborsAlways(pos, component);
    }

    @Override
    public void updateNeighborsAlways(ComponentPos pos, Component sourceComponent) {
        this.neighborUpdater.updateNeighbors(pos, sourceComponent, null);
    }

    public void updateNeighborsExcept(ComponentPos pos, Component sourceComponent, FlatDirection direction) {
        this.neighborUpdater.updateNeighbors(pos, sourceComponent, direction);
    }

    public void updateNeighbor(ComponentPos pos, Component sourceComponent, ComponentPos sourcePos) {
        this.neighborUpdater.updateNeighbor(pos, sourceComponent, sourcePos);
    }

    public void updateNeighbor(ComponentState state, ComponentPos pos, Component sourceComponent, ComponentPos sourcePos, boolean notify) {
        this.neighborUpdater.updateNeighbor(state, pos, sourceComponent, sourcePos, notify);
    }
}
