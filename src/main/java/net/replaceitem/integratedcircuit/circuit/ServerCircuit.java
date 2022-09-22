package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.Direction;

import java.util.UUID;

public class ServerCircuit extends Circuit {
    
    protected final CircuitNeighborUpdater neighborUpdater;
    protected final CircuitTickScheduler circuitTickScheduler = new CircuitTickScheduler();
    
    protected final IntegratedCircuitBlockEntity blockEntity;
    private long tickOrder;
    
    public ServerCircuit(IntegratedCircuitBlockEntity blockEntity) {
        super();
        this.blockEntity = blockEntity;
        this.neighborUpdater = new CircuitNeighborUpdater(this);
    }

    public IntegratedCircuitBlockEntity getCircuitBlockEntity() {
        return blockEntity;
    }

    public CircuitTickScheduler getCircuitTickScheduler() {
        return this.circuitTickScheduler;
    }
    
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        for (Direction direction : Direction.VALUES) {
            int newPower = ((IntegratedCircuitBlock) state.getBlock()).getInputPower(world, pos, direction);
            this.ports[direction.toInt()].assignExternalPower(this, PORTS_GRID_POS[direction.toInt()], newPower);
        }
        this.circuitTickScheduler.tick(this.getTime(), 65536, this::tickBlock);

        IntegratedCircuitBlockEntity integratedCircuitBlockEntity = (IntegratedCircuitBlockEntity) blockEntity;
        boolean updateNeeded = false;
        for (Direction direction : Direction.VALUES) {
            int oldPower = integratedCircuitBlockEntity.getOutputStrength(direction);
            int newPower = this.ports[direction.toInt()].getInternalPower(this, PORTS_GRID_POS[direction.toInt()]);
            integratedCircuitBlockEntity.setOutputStrength(direction, newPower);
            if(oldPower != newPower) updateNeeded = true;
        }
        
        if(updateNeeded) {
            //world.setBlockState(pos, state, Block.NOTIFY_ALL);
            state.onBlockAdded(world, pos, state, true);
        }
    }

    public long getTime() {
        return this.blockEntity.getWorld().getTime();
    }

    private void tickBlock(ComponentPos pos, Component block) {
        ComponentState blockState = this.getComponentState(pos);
        if (blockState.isOf(block)) {
            blockState.scheduledTick(this, pos, this.blockEntity.getWorld().getRandom());
        }
    }

    public static ServerCircuit fromNbt(NbtCompound nbt, IntegratedCircuitBlockEntity blockEntity) {
        if(nbt == null) return null;
        ServerCircuit circuit = new ServerCircuit(blockEntity);
        circuit.readNbt(nbt);
        return circuit;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("tickScheduler", this.circuitTickScheduler.toNbt(this.getTime()));
    }

    private NbtList tickSchedulerNbtBuffer;

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtList tickSchedulerNbt = nbt.getList("tickScheduler", NbtElement.COMPOUND_TYPE);
        if(this.blockEntity.hasWorld()) {
            loadTickScheduler(tickSchedulerNbt);
        } else {
            // this has to be stored until the world is present, since the world time is needed to get the correct triggerTime for scheduled ticks
            this.tickSchedulerNbtBuffer = tickSchedulerNbt;
        }
    }

    public void onWorldIsPresent() {
        if(this.tickSchedulerNbtBuffer == null) return;
        loadTickScheduler(tickSchedulerNbtBuffer);
        this.tickSchedulerNbtBuffer = null;
    }
    
    private void loadTickScheduler(NbtList list) {
        this.circuitTickScheduler.loadFromNbt(list, this.getTime());
    }

    public void placeComponentFromClient(ComponentPos pos, Component component, Direction rotation) {

        ComponentState placementState = component.getPlacementState(this, pos, rotation);
        if(placementState == null) placementState = Components.AIR_DEFAULT_STATE;
        
        ComponentState beforeState = this.getComponentState(pos);
        if(beforeState.isAir() && placementState.isAir()) return;
        this.setComponentState(pos, placementState, Component.NOTIFY_ALL);
        placementState.getComponent().onPlaced(this, pos, placementState);
    }

    @Override
    public boolean setComponentState(ComponentPos pos, ComponentState state, int flags) {
        // maybe check before state and only update on change?
        ComponentState beforeState = getComponentState(pos);
        if(!super.setComponentState(pos, state, flags)) return false;
        
        handleComponentStateSet(beforeState, state, pos, flags);
        
        return true;
    }

    @Override
    public void setPortComponentState(ComponentPos pos, PortComponentState newState, int flags) {
        ComponentState beforeState = getComponentState(pos);
        super.setPortComponentState(pos, newState, flags);
        handleComponentStateSet(beforeState, newState, pos, flags);
    }

    // handles all the updating of stuffs
    private void handleComponentStateSet(ComponentState beforeState, ComponentState state, ComponentPos pos, int flags) {
        updateClient(pos, state);

        beforeState.onStateReplaced(this, pos, state);
        state.onBlockAdded(this, pos, beforeState);

        if ((flags & Component.NOTIFY_NEIGHBORS) != 0) {
            this.updateNeighbors(pos, state.getComponent());
        }
        if ((flags & Component.FORCE_STATE) == 0) {
            int newFlags = flags & ~(Component.NOTIFY_NEIGHBORS | Component.SKIP_DROPS);
            //blockState.prepare(this, pos, i, maxUpdateDepth - 1);
            state.updateNeighbors(this, pos, newFlags);
            //state.prepare(this, pos, i, maxUpdateDepth - 1);
        }

        blockEntity.markDirty();

    }

    public int getReceivedRedstonePower(ComponentPos pos) {
        int i = 0;
        for (Direction direction : Direction.VALUES) {
            int j = this.getEmittedRedstonePower(pos.offset(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    public int getEmittedRedstonePower(ComponentPos pos, Direction direction) {
        ComponentState blockState = this.getComponentState(pos);
        int i = blockState.getWeakRedstonePower(this, pos, direction);
        if (blockState.isSolidBlock(this, pos)) {
            return Math.max(i, this.getReceivedStrongRedstonePower(pos));
        }
        return i;
    }

    public boolean isEmittingRedstonePower(ComponentPos pos, Direction direction) {
        return this.getEmittedRedstonePower(pos, direction) > 0;
    }

    private int getReceivedStrongRedstonePower(ComponentPos pos) {
        int i = 0;
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.north(), Direction.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.south(), Direction.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.west(), Direction.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.east(), Direction.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    public boolean isReceivingRedstonePower(ComponentPos pos) {
        if (this.getEmittedRedstonePower(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getEmittedRedstonePower(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getEmittedRedstonePower(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getEmittedRedstonePower(pos.east(), Direction.EAST) > 0;
    }

    public int getStrongRedstonePower(ComponentPos pos, Direction direction) {
        return this.getComponentState(pos).getStrongRedstonePower(this, pos, direction);
    }

    private void updateClient(ComponentPos pos, ComponentState state) {
        UUID editorUUID = this.blockEntity.getEditor();
        if(editorUUID == null) return;
        ServerPlayerEntity editor = this.blockEntity.getWorld().getServer().getPlayerManager().getPlayer(this.blockEntity.getEditor());
        if(editor == null) return;
        new ComponentUpdateS2CPacket(pos, state).send(editor);
    }

    public void cycleState(ComponentPos pos) {
        if(isPort(pos)) {
            int portNumber = getPortNumber(pos);
            Direction portSide = Direction.VALUES[portNumber];
            PortComponentState portComponentState = ports[portNumber];
            Direction newRotation = portComponentState.getRotation() == portSide ? portSide.getOpposite() : portSide;
            setPortComponentState(pos, new PortComponentState(newRotation, (byte) 0, newRotation == portSide), Component.NOTIFY_ALL);
        } else {
            ComponentState state = this.getComponentState(pos);
            state.cycleState(this, pos);
        }
    }

    public void createAndScheduleBlockTick(ComponentPos pos, Component component, int delay, TickPriority priority) {
        this.getCircuitTickScheduler().scheduleTick(this.createOrderedTick(pos, component, delay, priority));
    }

    public void createAndScheduleBlockTick(ComponentPos pos, Component component, int delay) {
        this.getCircuitTickScheduler().scheduleTick(this.createOrderedTick(pos, component, delay));
    }

    private OrderedCircuitTick createOrderedTick(ComponentPos pos, Component type, int delay, TickPriority priority) {
        return new OrderedCircuitTick(type, pos, this.getTime() + (long)delay, priority, this.getTickOrder());
    }

    private OrderedCircuitTick createOrderedTick(ComponentPos pos, Component type, int delay) {
        return new OrderedCircuitTick(type, pos, this.getTime() + (long)delay, this.getTickOrder());
    }

    public long getTickOrder() {
        return this.tickOrder++;
    }

    public void updateNeighbors(ComponentPos pos, Component component) {
        this.updateNeighborsAlways(pos, component);
    }

    public void updateNeighborsAlways(ComponentPos pos, Component sourceComponent) {
        this.neighborUpdater.updateNeighbors(pos, sourceComponent, null);
    }

    public void updateNeighborsExcept(ComponentPos pos, Component sourceComponent, Direction direction) {
        this.neighborUpdater.updateNeighbors(pos, sourceComponent, direction);
    }

    public void updateNeighbor(ComponentPos pos, Component sourceComponent, ComponentPos sourcePos) {
        this.neighborUpdater.updateNeighbor(pos, sourceComponent, sourcePos);
    }

    public void updateNeighbor(ComponentState state, ComponentPos pos, Component sourceComponent, ComponentPos sourcePos, boolean notify) {
        this.neighborUpdater.updateNeighbor(state, pos, sourceComponent, sourcePos, notify);
    }

    public void replaceWithStateForNeighborUpdate(Direction direction, ComponentState neighborState, ComponentPos pos, ComponentPos neighborPos, int flags) {
        this.neighborUpdater.replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags);
    }
}
