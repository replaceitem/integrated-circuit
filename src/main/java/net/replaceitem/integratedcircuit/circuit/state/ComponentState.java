package net.replaceitem.integratedcircuit.circuit.state;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.util.Direction;

public class ComponentState {

    protected Component component;

    public ComponentState(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
    
    public boolean isComponent(Component component) {
        return this.component.equals(component);
    }

    public boolean isAir() {
        return isComponent(Components.AIR);
    }

    public byte encodeStateData() {
        return 0;
    }

    public short encode() {
        return (short) (this.encodeStateData() << 8 | component.getId() & 0xFF);
    }

    public void cycleState(ServerCircuit circuit, ComponentPos pos) {
        this.component.onUse(this, circuit, pos);
    }

    public ComponentState copy() {
        return Components.createComponentState(this.encode());
    }

    @Override
    public boolean equals(Object obj) { // this should be used instead where minecraft uses == (minecraft's states have only a single instance per possible state in a table (I think?))
        return obj instanceof ComponentState other && this.encode() == other.encode();
    }

    public boolean emitsRedstonePower() {
        return this.component.emitsRedstonePower(this);
    }

    /**
     * {@link AbstractBlock.AbstractBlockState#getStateForNeighborUpdate(net.minecraft.util.math.Direction, BlockState, WorldAccess, BlockPos, BlockPos)} 
     */
    public ComponentState getStateForNeighborUpdate(Direction direction, ComponentState neighborState, ServerCircuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return this.component.getStateForNeighborUpdate(this, direction, neighborState, circuit, pos, neighborPos);
    }

    /**
     * {@link AbstractBlock.AbstractBlockState#neighborUpdate(World, BlockPos, Block, BlockPos, boolean)}
     */
    public void neighborUpdate(ServerCircuit world, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        this.component.neighborUpdate(this, world, pos, sourceBlock, sourcePos, notify);
    }

    public void updateNeighbors(ServerCircuit circuit, ComponentPos pos, int flags) {
        for (Direction direction : Component.DIRECTIONS) {
            ComponentPos offsetPos = pos.offset(direction);
            circuit.replaceWithStateForNeighborUpdate(direction.getOpposite(), this, offsetPos, pos, flags);
        }
    }
    
    public void onStateReplaced(ServerCircuit circuit, ComponentPos pos, ComponentState newState) {
        this.component.onStateReplaced(this, circuit, pos, newState);
    }

    public void scheduledTick(ServerCircuit circuit, ComponentPos pos, Random random) {
        this.getComponent().scheduledTick(this, circuit, pos, random);
    }

    public void onBlockAdded(ServerCircuit circuit, ComponentPos pos, ComponentState oldState) {
        this.component.onBlockAdded(this, circuit, pos, oldState);
    }

    public boolean isOf(Component component) {
        return this.component == component;
    }

    public int getWeakRedstonePower(ServerCircuit circuit, ComponentPos pos, Direction direction) {
        return this.component.getWeakRedstonePower(this, circuit, pos, direction);
    }

    public int getStrongRedstonePower(ServerCircuit circuit, ComponentPos pos, Direction direction) {
        return this.component.getStrongRedstonePower(this, circuit, pos, direction);
    }

    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return this.component.isSolidBlock(circuit, pos);
    }

    public boolean canPlaceAt(Circuit circuit, ComponentPos pos) {
        return this.component.canPlaceAt(this, circuit, pos);
    }
}
