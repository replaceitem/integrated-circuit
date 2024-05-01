package net.replaceitem.integratedcircuit.circuit;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public abstract class Circuit implements CircuitAccess {
    public static final int SIZE = 15;
    
    public static final BiMap<FlatDirection, ComponentPos> PORT_POSITIONS = EnumHashBiMap.create(FlatDirection.class);
    
    static {
        PORT_POSITIONS.put(FlatDirection.NORTH, new ComponentPos(7, -1));
        PORT_POSITIONS.put(FlatDirection.EAST, new ComponentPos(15, 7));
        PORT_POSITIONS.put(FlatDirection.SOUTH, new ComponentPos(7, 15));
        PORT_POSITIONS.put(FlatDirection.WEST, new ComponentPos(-1, 7));
    }
    
    protected final CircuitSection section;
    protected final ComponentState[] ports;

    protected final CircuitNeighborUpdater neighborUpdater = new CircuitNeighborUpdater(this);
    
    /**
     * @see net.minecraft.world.World#isClient
     */
    public final boolean isClient;
    private long tickOrder;

    public Circuit(boolean isClient) {
        this.isClient = isClient;
        this.ports = createDefaultPorts();
        this.section = new CircuitSection();
    }
    
    public Circuit(boolean isClient, ComponentState[] portStates, CircuitSection section) {
        this.isClient = isClient;
        this.ports = portStates;
        this.section = section;
    }

    public ComponentState[] getPorts() {
        return ports;
    }

    public CircuitSection getSection() {
        return section;
    }

    public static ComponentState[] createDefaultPorts() {
        ComponentState[] ports = new ComponentState[4];
        for (int i = 0; i < ports.length; i++) {
            ports[i] = Components.PORT.getDefaultState().with(PortComponent.FACING, FlatDirection.VALUES[i].getOpposite());
        }
        return ports;
    }

    public boolean isInside(ComponentPos pos) {
        return pos.getX() >= 0 && pos.getX() < SIZE && pos.getY() >= 0 && pos.getY() < SIZE;
    }
    
    public abstract long getTime();

    public boolean isValidPos(ComponentPos pos) {
        return isInside(pos) || isPortPos(pos);
    }

    public ComponentState getComponentState(ComponentPos componentPos) {
        if (isInside(componentPos)) {
            return this.section.getComponentState(componentPos.getX(), componentPos.getY());
        }
        FlatDirection portSide = getPortSide(componentPos);
        if(portSide != null) {
            return ports[portSide.getIndex()];
        }
        return Components.AIR.getDefaultState();
    }

    /**
     * Handles directly setting the component state, without any updates.
     * Assumes {@code pos} is already valid
     * Equivalent to {@link net.minecraft.world.chunk.ChunkSection#setBlockState(int, int, int, BlockState)}
     * @return The old component state before placement.
     */
    protected ComponentState assignComponentState(ComponentPos pos, ComponentState state) {
        ComponentState oldState = getComponentState(pos);
        FlatDirection portSide = getPortSide(pos);
        if(portSide != null) {
            if(!state.isOf(Components.PORT)) throw new RuntimeException("Cannot place non-port component at a port location");
            ports[portSide.getIndex()] = state;
        } else {
            this.section.setComponentState(pos, state);
        }
        return oldState;
    }

    public boolean setComponentState(ComponentPos pos, ComponentState state, int flags) {
        return this.setComponentState(pos, state, flags, 512);
    }

    /**
     * @see net.minecraft.world.World#setBlockState(BlockPos, BlockState, int)
     */
    public boolean setComponentState(ComponentPos pos, ComponentState state, int flags, int maxUpdateDepth) {
        if(!isValidPos(pos)) return false;
        if(state == null) state = Components.AIR_DEFAULT_STATE;

        // WorldChunk.setBlockState enters here in World.setBlockState
        ComponentState oldState = assignComponentState(pos, state);
        if(oldState == state) return false;
        if(!this.isClient) {
            oldState.onStateReplaced(this, pos, state);
        }
        if(!this.isClient) {
            state.onBlockAdded(this, pos, oldState);
        }
        // ends here


        ComponentState placedComponentState = this.getComponentState(pos);
        if (placedComponentState == state) {
            if ((flags & Block.NOTIFY_LISTENERS) != 0 && (!this.isClient || (flags & Block.NO_REDRAW) == 0)) {
                this.updateListeners(pos, oldState, state, flags);
            }
            if ((flags & Component.NOTIFY_NEIGHBORS) != 0) {
                this.updateNeighbors(pos, oldState.getComponent());
            }
            if ((flags & Block.FORCE_STATE) == 0 && maxUpdateDepth > 0) {
                int i = flags & ~(Block.NOTIFY_NEIGHBORS | Block.SKIP_DROPS);
                oldState.prepare(this, pos, i, maxUpdateDepth - 1);
                state.updateNeighbors(this, pos, i, maxUpdateDepth - 1);
                state.prepare(this, pos, i, maxUpdateDepth - 1);
            }
        }
        return true;
    }

    public static @Nullable FlatDirection getPortSide(ComponentPos pos) {
        return PORT_POSITIONS.inverse().get(pos);
    }

    public static boolean isPortPos(ComponentPos pos) {
        return getPortSide(pos) != null;
    }

    public boolean isEmpty() {
        for (int i = 0; i < ports.length; i++) {
            if(ports[i].get(PortComponent.FACING) != FlatDirection.VALUES[i].getOpposite()) {
                return false;
            }
        }
        return section.isEmpty();
    }

    protected abstract void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags);

    @Override
    public void replaceWithStateForNeighborUpdate(FlatDirection direction, ComponentState neighborState, ComponentPos pos, ComponentPos neighborPos, int flags, int maxUpdateDepth) {
        this.neighborUpdater.replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags);
    }


    public int getEmittedRedstonePower(ComponentPos pos, FlatDirection direction) {
        ComponentState blockState = this.getComponentState(pos);
        int i = blockState.getWeakRedstonePower(this, pos, direction);
        if (blockState.isSolidBlock(this, pos)) {
            return Math.max(i, this.getReceivedStrongRedstonePower(pos));
        }
        return i;
    }

    public boolean isEmittingRedstonePower(ComponentPos pos, FlatDirection direction) {
        return this.getEmittedRedstonePower(pos, direction) > 0;
    }


    private int getReceivedStrongRedstonePower(ComponentPos pos) {
        int i = 0;
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.north(), FlatDirection.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.south(), FlatDirection.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.west(), FlatDirection.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getStrongRedstonePower(pos.east(), FlatDirection.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    public int getStrongRedstonePower(ComponentPos pos, FlatDirection direction) {
        return this.getComponentState(pos).getStrongRedstonePower(this, pos, direction);
    }

    public int getReceivedRedstonePower(ComponentPos pos) {
        int i = 0;
        for (FlatDirection direction : FlatDirection.VALUES) {
            int j = this.getEmittedRedstonePower(pos.offset(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    public boolean isReceivingRedstonePower(ComponentPos pos) {
        if (this.getEmittedRedstonePower(pos.north(), FlatDirection.NORTH) > 0) {
            return true;
        }
        if (this.getEmittedRedstonePower(pos.south(), FlatDirection.SOUTH) > 0) {
            return true;
        }
        if (this.getEmittedRedstonePower(pos.west(), FlatDirection.WEST) > 0) {
            return true;
        }
        return this.getEmittedRedstonePower(pos.east(), FlatDirection.EAST) > 0;
    }

    public void useComponent(ComponentPos pos, PlayerEntity player) {
        ComponentState state = this.getComponentState(pos);
        state.onUse(this, pos, player);
    }


    /**
     * @see net.minecraft.world.World#removeBlock(BlockPos, boolean)
     */
    public boolean removeBlock(ComponentPos pos) {
        return this.setComponentState(pos, Components.AIR_DEFAULT_STATE, Block.NOTIFY_ALL);
    }

    public abstract void placeComponentState(ComponentPos pos, Component component, FlatDirection placementRotation);

    public long getTickOrder() {
        return this.tickOrder++;
    }


    /**
     * @see net.minecraft.world.World#breakBlock(BlockPos, boolean)
     */
    public boolean breakBlock(ComponentPos pos) {
        return breakBlock(pos, 512);
    }
    
    public boolean breakBlock(ComponentPos pos, int maxUpdateDepth) {
        ComponentState blockState = this.getComponentState(pos);
        if (blockState.isAir()) {
            return false;
        }
        return this.setComponentState(pos, Components.AIR_DEFAULT_STATE, Component.NOTIFY_ALL, maxUpdateDepth);
    }
    
    public final void playSound(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        playSoundInternal(except, sound, category, volume, pitch * 1.6f);
    }
    
    protected abstract void playSoundInternal(@Nullable PlayerEntity except, SoundEvent sound, SoundCategory category, float volume, float pitch);
}
