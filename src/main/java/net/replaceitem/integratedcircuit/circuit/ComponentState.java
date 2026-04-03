package net.replaceitem.integratedcircuit.circuit;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class ComponentState extends StateHolder<Component, ComponentState> {

    public static final Codec<ComponentState> CODEC = codec(IntegratedCircuit.COMPONENTS_REGISTRY.byNameCodec(), Component::getDefaultState, Component::getStateDefinition).stable();

    public ComponentState(Component owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(owner, propertyKeys, propertyValues);
    }

    public Component getComponent() {
        return owner;
    }

    public boolean isAir() {
        return isOf(Components.AIR);
    }

    public boolean isOf(Component component) {
        return this.owner == component;
    }

    public void onUse(Circuit circuit, ComponentPos pos, Player player) {
        this.owner.onUse(this, circuit, pos, player);
    }

    public boolean emitsRedstonePower() {
        return this.owner.emitsRedstonePower(this);
    }

    public ComponentState getStateForNeighborUpdate(FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return this.owner.getStateForNeighborUpdate(this, direction, neighborState, circuit, pos, neighborPos);
    }

    public void neighborUpdate(Circuit world, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        this.owner.neighborUpdate(this, world, pos, sourceBlock, sourcePos, notify);
    }

    public void updateNeighbors(CircuitAccess circuit, ComponentPos pos, int flags) {
        updateNeighbors(circuit, pos, flags, 512);
    }

    public void updateNeighbors(CircuitAccess circuit, ComponentPos pos, int flags, int maxUpdateDepth) {
        for (FlatDirection direction : Component.DIRECTIONS) {
            ComponentPos offsetPos = pos.offset(direction);
            circuit.replaceWithStateForNeighborUpdate(direction.getOpposite(), this, offsetPos, pos, flags, maxUpdateDepth);
        }
    }

    public void onStateReplaced(Circuit circuit, ComponentPos pos, ComponentState newState) {
        this.owner.onStateReplaced(this, circuit, pos, newState);
    }

    public void scheduledTick(ServerCircuit circuit, ComponentPos pos, RandomSource random) {
        this.owner.scheduledTick(this, circuit, pos, random);
    }

    public void onBlockAdded(Circuit circuit, ComponentPos pos, ComponentState oldState) {
        this.owner.onBlockAdded(this, circuit, pos, oldState);
    }

    public void prepare(CircuitAccess circuit, ComponentPos pos, int flags) {
        this.prepare(circuit, pos, flags, 512);
    }
    public void prepare(CircuitAccess circuit, ComponentPos pos, int flags, int maxUpdateDepth) {
        this.owner.prepare(this, circuit, pos, flags, maxUpdateDepth);
    }

    public int getWeakRedstonePower(Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.owner.getWeakRedstonePower(this, circuit, pos, direction);
    }

    public int getStrongRedstonePower(Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.owner.getStrongRedstonePower(this, circuit, pos, direction);
    }

    public int increasePower(FlatDirection side) {
        return this.owner.increasePower(this, side);
    }

    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return this.owner.isSolidBlock(circuit, pos);
    }

    public boolean canPlaceAt(Circuit circuit, ComponentPos pos) {
        return this.owner.canPlaceAt(this, circuit, pos);
    }

    public net.minecraft.network.chat.Component getHoverInfoText() {
        return this.owner.getHoverInfoText(this);
    }

    public boolean hasComparatorOutput() {
        return this.owner.hasComparatorOutput(this);
    }

    public int getComparatorOutput(Circuit circuit, ComponentPos pos) {
        return this.owner.getComparatorOutput(this, circuit, pos);
    }
}
