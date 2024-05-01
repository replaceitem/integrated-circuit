package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerCircuit extends Circuit {
    private final ServerCircuitContext context;
    protected final CircuitTickScheduler circuitTickScheduler;

    public ServerCircuit(ServerCircuitContext context) {
        super(false);
        this.context = context;
        this.circuitTickScheduler = new CircuitTickScheduler();
    }

    public ServerCircuit(@NotNull ServerCircuitContext context, @NotNull ComponentState[] portStates, @NotNull CircuitSection section, @NotNull CircuitTickScheduler tickScheduler) {
        super(false, portStates, section);
        this.context = context;
        this.circuitTickScheduler = tickScheduler;
    }

    @Override
    public CircuitTickScheduler getCircuitTickScheduler() {
        return this.circuitTickScheduler;
    }

    public ServerCircuitContext getContext() {
        return context;
    }

    public void tick() {
        this.circuitTickScheduler.tick(this.getTime(), 65536, this::tickBlock);
        context.markDirty(); // TODO - Cheating for now
    }

    private void tickBlock(ComponentPos pos, Component block) {
        ComponentState blockState = this.getComponentState(pos);
        if (blockState.isOf(block)) {
            blockState.scheduledTick(this, pos, this.context.getRandom());
        }
    }
    
    public void onExternalPowerChanged(FlatDirection direction, int power) {
        ComponentPos pos = PORT_POSITIONS.get(direction);
        ComponentState state = getComponentState(pos);
        boolean isOutput = state.get(PortComponent.IS_OUTPUT);
        if(!isOutput && state.get(PortComponent.POWER) != power) {
            setComponentState(pos, state.with(PortComponent.POWER, power), Component.NOTIFY_ALL);
        }
    }

    public int getPortOutputStrength(FlatDirection direction) {
        ComponentPos pos = PORT_POSITIONS.get(direction);
        ComponentState state = getComponentState(pos);
        if(!state.get(PortComponent.IS_OUTPUT)) return 0;
        return state.get(PortComponent.POWER);
    }

    @Override
    public long getTime() {
        return context.getTime();
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
    public void playSoundInternal(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.context.playSound(except, sound, category, volume, pitch);
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
