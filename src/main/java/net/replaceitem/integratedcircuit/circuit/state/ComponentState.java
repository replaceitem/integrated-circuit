package net.replaceitem.integratedcircuit.circuit.state;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.replaceitem.integratedcircuit.circuit.*;
import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Set;

public class ComponentState extends AbstractComponentState {

    public ComponentState(Component component, byte data) {
        super(data, component);
    }

    @Override
    protected Set<ComponentProperty<?>> getProperties() {
        return component.getProperties();
    }

    @Override
    protected int getComponentId() {
        return component.getId();
    }

    public Component getComponent() {
        return component;
    }

    public boolean isAir() {
        return isOf(Components.AIR);
    }

    public boolean isOf(Component component) {
        return this.component == component;
    }

    public <T> T get(ComponentProperty<T> property) {
        assertValidProperty(property);
        return this.propertyMap.get(property);
    }

    public <T> ComponentState with(ComponentProperty<T> property, T value) {
        assertValidProperty(property);
        PropertyMap newPropertyMap = this.propertyMap.with(property, value);
        byte newData = newPropertyMap.encode();
        return component.getState(newData);
    }

    public <T> ComponentState cycle(ComponentProperty<T> property) {
        return with(property, property.cycle(this.get(property)));
    }

    private void assertValidProperty(ComponentProperty<?> property) {
        if(!this.component.getProperties().contains(property)) throw new RuntimeException(String.format("%s '%s' cannot be applied to component '%s'", property.getClass().getSimpleName(), property, this.component));
    }






    public void onUse(Circuit circuit, ComponentPos pos, PlayerEntity player) {
        this.component.onUse(this, circuit, pos, player);
    }

    @Deprecated(forRemoval = true)
    public ComponentState copy() {
        return Components.createComponentState(this.encode());
    }


    @Override
    public boolean equals(Object obj) { // this should be used instead where minecraft uses == (minecraft's states have only a single instance per possible state in a table (I think?))
        return this == obj;
    }

    public boolean emitsRedstonePower() {
        return this.component.emitsRedstonePower(this);
    }

    /**
     * {@link AbstractBlock.AbstractBlockState#getStateForNeighborUpdate(net.minecraft.util.math.Direction, BlockState, WorldAccess, BlockPos, BlockPos)}
     */
    public ComponentState getStateForNeighborUpdate(FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return this.component.getStateForNeighborUpdate(this, direction, neighborState, circuit, pos, neighborPos);
    }

    /**
     * {@link AbstractBlock.AbstractBlockState#neighborUpdate(World, BlockPos, Block, BlockPos, boolean)}
     */
    public void neighborUpdate(Circuit world, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        this.component.neighborUpdate(this, world, pos, sourceBlock, sourcePos, notify);
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
        this.component.onStateReplaced(this, circuit, pos, newState);
    }

    public void scheduledTick(ServerCircuit circuit, ComponentPos pos, Random random) {
        this.getComponent().scheduledTick(this, circuit, pos, random);
    }

    public void onBlockAdded(Circuit circuit, ComponentPos pos, ComponentState oldState) {
        this.component.onBlockAdded(this, circuit, pos, oldState);
    }


    public void prepare(CircuitAccess circuit, ComponentPos pos, int flags) {
        this.prepare(circuit, pos, flags, 512);
    }
    public void prepare(CircuitAccess circuit, ComponentPos pos, int flags, int maxUpdateDepth) {
        this.component.prepare(this, circuit, pos, flags, maxUpdateDepth);
    }

    public int getWeakRedstonePower(Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.component.getWeakRedstonePower(this, circuit, pos, direction);
    }

    public int getStrongRedstonePower(Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.component.getStrongRedstonePower(this, circuit, pos, direction);
    }

    public int increasePower(FlatDirection side) {
        return this.component.increasePower(this, side);
    }

    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return this.component.isSolidBlock(circuit, pos);
    }

    public boolean canPlaceAt(Circuit circuit, ComponentPos pos) {
        return this.component.canPlaceAt(this, circuit, pos);
    }

    public Text getHoverInfoText() {
        return this.component.getHoverInfoText(this);
    }
}
