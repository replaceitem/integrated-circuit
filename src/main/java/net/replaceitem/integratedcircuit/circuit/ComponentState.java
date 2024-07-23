package net.replaceitem.integratedcircuit.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public class ComponentState extends State<Component,ComponentState> {

    public static final Codec<ComponentState> CODEC = createCodec(IntegratedCircuit.COMPONENTS_REGISTRY.getCodec(), Component::getDefaultState).stable();
    
    protected ComponentState(Component owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> entries, MapCodec<ComponentState> codec) {
        super(owner, entries, codec);
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

    public void onUse(Circuit circuit, ComponentPos pos, PlayerEntity player) {
        this.owner.onUse(this, circuit, pos, player);
    }

    public boolean emitsRedstonePower() {
        return this.owner.emitsRedstonePower(this);
    }

    /**
     * {@link AbstractBlock.AbstractBlockState#getStateForNeighborUpdate(net.minecraft.util.math.Direction, BlockState, WorldAccess, BlockPos, BlockPos)}
     */
    public ComponentState getStateForNeighborUpdate(FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return this.owner.getStateForNeighborUpdate(this, direction, neighborState, circuit, pos, neighborPos);
    }

    /**
     * {@link AbstractBlock.AbstractBlockState#neighborUpdate(World, BlockPos, Block, BlockPos, boolean)}
     */
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

    public void scheduledTick(ServerCircuit circuit, ComponentPos pos, Random random) {
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

    public Text getHoverInfoText() {
        return this.owner.getHoverInfoText(this);
    }

    public boolean hasComparatorOutput() {
        return this.owner.hasComparatorOutput(this);
    }

    public int getComparatorOutput(Circuit circuit, ComponentPos pos) {
        return this.owner.getComparatorOutput(this, circuit, pos);
    }
}
