package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Arrays;

public abstract class Circuit implements CircuitAccess {
    public static final int SIZE = 15;
    
    public static final ComponentPos[] PORTS_GRID_POS = new ComponentPos[]{
            new ComponentPos(7, -1),
            new ComponentPos(15, 7),
            new ComponentPos(7, 15),
            new ComponentPos(-1, 7)
    };

    public final ComponentState[][] components = new ComponentState[SIZE][SIZE];
    public final PortComponentState[] ports = new PortComponentState[4];



    protected final CircuitNeighborUpdater neighborUpdater;

    /**
     * @see net.minecraft.world.World#isClient
     */
    public final boolean isClient;
    private long tickOrder;

    public Circuit(boolean isClient) {
        this.isClient = isClient;
        for (ComponentState[] componentState : components) {
            Arrays.fill(componentState, Components.AIR.getDefaultState());
        }
        for (int i = 0; i < ports.length; i++) {
            ports[i] = new PortComponentState(FlatDirection.VALUES[i].getOpposite(), (byte) 0, false);
        }

        this.neighborUpdater = new CircuitNeighborUpdater(this);
    }

    public boolean isInside(ComponentPos pos) {
        return pos.getX() >= 0 && pos.getX() < SIZE && pos.getY() >= 0 && pos.getY() < SIZE;
    }

    public boolean isValidPos(ComponentPos pos) {
        return isInside(pos) || isPort(pos);
    }

    public ComponentState getComponentState(ComponentPos componentPos) {
        if (isInside(componentPos)) {
            return this.components[componentPos.getX()][componentPos.getY()];
        }
        if(isPort(componentPos)) {
            return ports[getPortNumber(componentPos)];
        }
        return Components.AIR.getDefaultState();
    }

    /**
     * Handles directly setting the component state, without any updates.
     * Equivalent to {@link net.minecraft.world.chunk.ChunkSection#setBlockState(int, int, int, BlockState)}
     * @return The old component state before placement.
     */
    private ComponentState assignComponentState(ComponentPos pos, ComponentState state) {
        ComponentState oldState = getComponentState(pos);
        if(isPort(pos) && state instanceof PortComponentState portComponentState) {
            ports[getPortNumber(pos)] = portComponentState;
        } else {
            this.components[pos.getX()][pos.getY()] = state;
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
        if(oldState.equals(state)) return false;
        if(!this.isClient) {
            oldState.onStateReplaced(this, pos, state);
        }
        if(!this.isClient) {
            state.onBlockAdded(this, pos, oldState);
        }
        // ends here


        ComponentState placedComponentState = this.getComponentState(pos);
        if (placedComponentState.equals(state)) {
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

    public int getPortNumber(ComponentPos pos) {
        for (int i = 0; i < 4; i++) {
            if(PORTS_GRID_POS[i].equals(pos)) return i;
        }
        return -1;
    }

    public boolean isPort(ComponentPos pos) {
        return getPortNumber(pos) != -1;
    }

    public void writeNbt(NbtCompound nbt) {
        byte[] portBytes = new byte[4];
        for (int i = 0; i < ports.length; i++) {
            portBytes[i] = ports[i].encodeStateData();
        }
        nbt.putByteArray("ports", portBytes);

        //packing two shorts in an int
        int componentDataSize = SIZE*SIZE;
        int[] componentsData = new int[MathHelper.ceilDiv(componentDataSize, 2)];
        for (int i = 0; i < componentDataSize; i++) {
            int shift = (i%2==0)?16:0;
            componentsData[i/2] |= (components[i%SIZE][i/SIZE].encode() & 0xFFFF) << shift;
        }
        nbt.putIntArray("components", componentsData);
    }

    public void readNbt(NbtCompound nbt) {
        byte[] portBytes = nbt.getByteArray("ports");
        for (int i = 0; i < portBytes.length; i++) {
            ports[i] = (PortComponentState) Components.PORT.getState(portBytes[i]);
        }
        int[] componentData = nbt.getIntArray("components");
        int componentDataSize = SIZE*SIZE;
        if(componentData.length != MathHelper.ceilDiv(componentDataSize, 2)) throw new RuntimeException("Invalid componentData length received");
        for (int i = 0; i < componentDataSize; i++) {
            int shift = (i%2==0)?16:0;
            components[i % SIZE][i / SIZE] = Components.createComponentState((short) (componentData[i / 2] >> shift & 0xFFFF));
        }
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    protected abstract void updateListeners(ComponentPos pos, ComponentState oldState, ComponentState state, int flags);

    @Override
    public void replaceWithStateForNeighborUpdate(Direction direction, ComponentState neighborState, ComponentPos pos, ComponentPos neighborPos, int flags, int maxUpdateDepth) {
        this.neighborUpdater.replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags);
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

    public int getStrongRedstonePower(ComponentPos pos, Direction direction) {
        return this.getComponentState(pos).getStrongRedstonePower(this, pos, direction);
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

    public void useComponent(ComponentPos pos, BlockPos blockPos) {
        ComponentState state = this.getComponentState(pos);
        state.onUse(this, pos);
    }


    /**
     * @see net.minecraft.world.World#removeBlock(BlockPos, boolean)
     */
    public boolean removeBlock(ComponentPos pos) {
        return this.setComponentState(pos, Components.AIR_DEFAULT_STATE, Block.NOTIFY_ALL);
    }

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
}
