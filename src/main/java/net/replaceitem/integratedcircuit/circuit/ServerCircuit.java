package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.network.packet.ComponentUpdateS2CPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Set;

public class ServerCircuit extends Circuit {

    protected final CircuitTickScheduler circuitTickScheduler = new CircuitTickScheduler();
    
    protected final IntegratedCircuitBlockEntity blockEntity;
    
    public ServerCircuit(IntegratedCircuitBlockEntity blockEntity) {
        super(false);
        this.blockEntity = blockEntity;
    }

    public IntegratedCircuitBlockEntity getCircuitBlockEntity() {
        return blockEntity;
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return this.circuitTickScheduler;
    }
    
    public void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            int newPower = ((IntegratedCircuitBlock) state.getBlock()).getInputPower(world, pos, direction);
            Components.PORT.assignExternalPower(this, PORTS_GRID_POS[direction.toInt()], this.ports[direction.toInt()], newPower);
        }
        this.circuitTickScheduler.tick(this.getTime(), 65536, this::tickBlock);

        IntegratedCircuitBlockEntity integratedCircuitBlockEntity = (IntegratedCircuitBlockEntity) blockEntity;
        boolean updateNeeded = false;
        for (FlatDirection direction : FlatDirection.VALUES) {
            int oldPower = integratedCircuitBlockEntity.getOutputStrength(direction);
            int newPower = Components.PORT.getInternalPower(this, PORTS_GRID_POS[direction.toInt()], this.ports[direction.toInt()]);
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

    public void placeComponentFromClient(ComponentPos pos, Component component, FlatDirection rotation) {

        ComponentState placementState = component.getPlacementState(this, pos, rotation);
        if(placementState == null) placementState = Components.AIR_DEFAULT_STATE;
        
        ComponentState beforeState = this.getComponentState(pos);
        if(beforeState.isAir() && placementState.isAir()) return;
        this.setComponentState(pos, placementState, Component.NOTIFY_ALL);
        placementState.getComponent().onPlaced(this, pos, placementState);
    }

    @Override
    protected void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags) {
        updateClient(pos, state);
    }

    private void updateClient(ComponentPos pos, ComponentState state) {
        World world = this.blockEntity.getWorld();
        if(world == null) return;
        MinecraftServer minecraftServer = world.getServer();
        if(minecraftServer == null) return;
        PlayerManager playerManager = minecraftServer.getPlayerManager();
        if(playerManager == null) return;
        Set<ServerPlayerEntity> editors = this.blockEntity.getEditingPlayers();
        new ComponentUpdateS2CPacket(pos, state).send(editors);
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
