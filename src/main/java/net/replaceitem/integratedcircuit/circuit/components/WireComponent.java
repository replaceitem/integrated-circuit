package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ObserverComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.circuit.state.RepeaterComponentState;
import net.replaceitem.integratedcircuit.circuit.state.WireComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;

import java.util.HashSet;

public class WireComponent extends Component {
    public WireComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.wire"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/item/redstone.png");

    private static final Identifier TEXTURE_DOT = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_dot.png");
    private static final Identifier TEXTURE_X = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_x.png");
    private static final Identifier TEXTURE_Y = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_y.png");


    boolean wiresGivePower = true;
    
    
    // these seem to be swapped in yarn mappings?!?!
    
    @Override
    public ComponentState getDefaultState() {
        return new WireComponentState((byte) 0b0000, (byte) 0);
    }

    private ComponentState getDotState() {
        return new WireComponentState((byte) 0b1111, (byte) 0);
    }
    
    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        return this.getPlacementState(circuit, this.getDotState(), pos);
    }

    @Override
    public ComponentState getState(byte data) {
        return new WireComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");

        final int size = IntegratedCircuitScreen.COMPONENT_SIZE;
        final int halfSize = size/2;

        Vec3d color = RedstoneWireBlockAccessor.getCOLORS()[wireComponentState.getPower()];
        float r = (float) color.x;
        float g = (float) color.y;
        float b = (float) color.z;

        if(wireComponentState.isConnected(FlatDirection.NORTH)) IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_Y, x, y, 0, r, g, b, a, 0, 0, size, halfSize);
        if(wireComponentState.isConnected(FlatDirection.EAST)) IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_X, x, y, 0, r, g, b, a, halfSize, 0, halfSize, size);
        if(wireComponentState.isConnected(FlatDirection.SOUTH)) IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_Y, x, y, 0, r, g, b, a, 0, halfSize, size, halfSize);
        if(wireComponentState.isConnected(FlatDirection.WEST)) IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_X, x, y, 0, r, g, b, a, 0, 0, halfSize, size);

        int connections = 0;
        for (FlatDirection direction : FlatDirection.VALUES) if(wireComponentState.isConnected(direction)) connections++;
        if(connections != 2) IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_DOT, x, y, 0, r, g, b, a, 0, 0, size, size);
        
        if(!(wireComponentState.isConnected(FlatDirection.NORTH) && wireComponentState.isConnected(FlatDirection.SOUTH) || wireComponentState.isConnected(FlatDirection.EAST) && wireComponentState.isConnected(FlatDirection.WEST))) {
            IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_DOT, x, y, 0, r, g, b, a, 0, 0, size, size);
        }
    }



    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        update(circuit, pos, state);
    }

    private void update(Circuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        int i = getReceivedRedstonePower(circuit, pos);
        if (wireComponentState.getPower() != i) {
            if (circuit.getComponentState(pos).equals(state)) {
                WireComponentState newState = (WireComponentState) state.copy();
                newState.setPower(i);
                circuit.setComponentState(pos, newState, Component.NOTIFY_LISTENERS);
            }
            HashSet<ComponentPos> set = Sets.newHashSet();
            set.add(pos);
            for (FlatDirection direction : FlatDirection.VALUES) {
                set.add(pos.offset(direction));
            }
            for (ComponentPos blockPos : set) {
                circuit.updateNeighborsAlways(blockPos, this);
            }
        }
    }

    private void updateOffsetNeighbors(Circuit circuit, ComponentPos pos) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            this.updateNeighbors(circuit, pos.offset(direction));
        }
    }

    private void updateNeighbors(Circuit circuit, ComponentPos pos) {
        ComponentState componentState = circuit.getComponentState(pos);
        if (!(componentState.isOf(this) || componentState.isOf(Components.PORT))) {
            return;
        }
        circuit.updateNeighborsAlways(pos, this);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        boolean wireConnection = this.getRenderConnectionType(circuit, pos, direction);
        if (wireConnection == wireComponentState.isConnected(direction) && !isFullyConnected(state)) {
            WireComponentState newState = (WireComponentState) state.copy();
            newState.setConnected(direction, wireConnection);
            return newState;
        }
        return this.getPlacementState(circuit, ((WireComponentState)this.getDotState()).setPower(wireComponentState.getPower()).setConnected(direction, wireConnection), pos);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if (state.isOf(newState.getComponent())) {
            return;
        }
        super.onStateReplaced(state, circuit, pos, newState);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (isFullyConnected(state) || isNotConnected(state)) {
            WireComponentState newState = (WireComponentState) (isFullyConnected(state) ? this.getDefaultState() : this.getDotState());
            newState.setPower(wireComponentState.getPower());
            if (!(newState = (WireComponentState) this.getPlacementState(circuit, newState, pos)).equals(state)) {
                circuit.setComponentState(pos, newState, Block.NOTIFY_ALL);
                this.updateForNewState(circuit, pos, wireComponentState, newState);
            }
        }
    }

    private void updateForNewState(Circuit world, ComponentPos pos, WireComponentState oldState, WireComponentState newState) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            ComponentPos blockPos = pos.offset(direction);
            if (oldState.isConnected(direction) == newState.isConnected(direction) || !world.getComponentState(blockPos).isSolidBlock(world, blockPos)) continue;
            world.updateNeighborsExcept(blockPos, newState.getComponent(), direction.getOpposite());
        }
    }

    @Override
    public void prepare(ComponentState state, CircuitAccess circuit, ComponentPos pos, int flags, int maxUpdateDepth) {
        /* // I don't think this actually does anything in a 2D world
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        ComponentPos mutable;
        for (Direction direction : Direction.VALUES) {
            boolean wireConnection = wireComponentState.isConnected(direction);
            mutable = pos.offset(direction);
            if (!wireConnection || circuit.getComponentState(pos.offset(direction)).isOf(this)) continue;
            // move(down)
            mutable.set((Vec3i)pos, direction).move(net.minecraft.util.math.Direction.UP);
            BlockState blockState2 = circuit.getBlockState(mutable);
            if (!blockState2.isOf(this)) continue;
            Vec3i blockPos2 = mutable.offset(direction.getOpposite());
            circuit.replaceWithStateForNeighborUpdate(direction.getOpposite(), circuit.getComponentState((BlockPos)blockPos2), mutable, (BlockPos)blockPos2, flags);
        }*/
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    private ComponentState getPlacementState(Circuit circuit, ComponentState state, ComponentPos pos) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        boolean notConnected = isNotConnected(wireComponentState);
        wireComponentState = this.getDefaultWireState(circuit, ((WireComponentState)this.getDefaultState()).setPower(wireComponentState.getPower()), pos);
        if (notConnected && isNotConnected(wireComponentState)) {
            return wireComponentState;
        }
        boolean n = wireComponentState.isConnected(FlatDirection.NORTH);
        boolean e = wireComponentState.isConnected(FlatDirection.SOUTH);
        boolean s = wireComponentState.isConnected(FlatDirection.EAST);
        boolean w = wireComponentState.isConnected(FlatDirection.WEST);
        boolean ne = !n && !e;
        boolean sw = !s && !w;
        if (!w && ne) {
            wireComponentState = ((WireComponentState) wireComponentState.copy()).setConnected(FlatDirection.WEST, true);
        }
        if (!s && ne) {
            wireComponentState = ((WireComponentState) wireComponentState.copy()).setConnected(FlatDirection.EAST, true);
        }
        if (!n && sw) {
            wireComponentState = ((WireComponentState) wireComponentState.copy()).setConnected(FlatDirection.NORTH, true);
        }
        if (!e && sw) {
            wireComponentState = ((WireComponentState) wireComponentState.copy()).setConnected(FlatDirection.SOUTH, true);
        }
        return wireComponentState;
    }
    
    private WireComponentState getDefaultWireState(Circuit circuit, ComponentState state, ComponentPos pos) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        for (FlatDirection direction : FlatDirection.VALUES) {
            if (wireComponentState.isConnected(direction)) continue;
            boolean wireConnection = this.getRenderConnectionType(circuit, pos, direction);
            wireComponentState = ((WireComponentState) wireComponentState.copy()).setConnected(direction,wireConnection);
        }
        return wireComponentState;
    }

    private static boolean isNotConnected(ComponentState state) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        return wireComponentState.getConnections() == 0b0000;
    }

    private static boolean isFullyConnected(ComponentState state) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        return wireComponentState.getConnections() == 0b1111;
    }

    private boolean getRenderConnectionType(Circuit circuit, ComponentPos pos, FlatDirection direction) {
        ComponentPos blockPos = pos.offset(direction);
        ComponentState blockState = circuit.getComponentState(blockPos);
        return WireComponent.connectsTo(blockState, direction);
    }

    private static boolean connectsTo(ComponentState state, FlatDirection direction) {
        if (state.isOf(Components.WIRE)) {
            return true;
        }
        if (state.isOf(Components.REPEATER) && state instanceof RepeaterComponentState repeaterComponentState) {
            FlatDirection rotation = repeaterComponentState.getRotation();
            return rotation == direction || rotation.getOpposite() == direction;
        }
        if (state.isOf(Components.OBSERVER) && state instanceof ObserverComponentState observerComponentState) {
            return direction == observerComponentState.getRotation();
        }
        return state.emitsRedstonePower() && direction != null;
    }

    // maybe broke because major changes from RedstoneWireBlock#getReceivedRedstonePower
    private int getReceivedRedstonePower(Circuit world, ComponentPos pos) {
        this.wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        this.wiresGivePower = true;
        int j = 0;
        if (i < 15) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                ComponentPos blockPos = pos.offset(direction);
                ComponentState blockState = world.getComponentState(blockPos);
                j = Math.max(j, increasePower(blockState));
            }
        }
        return Math.max(i, j - 1);
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (!this.wiresGivePower) {
            return 0;
        }
        int i = wireComponentState.getPower();
        if (i == 0) {
            return 0;
        }
        if (((WireComponentState) this.getPlacementState(circuit, state, pos)).isConnected(direction.getOpposite())) {
            return i;
        }
        return 0;
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.getWeakRedstonePower(state, circuit, pos, direction);
    }

    // name doesn't make sense, but RedstoneWireBlock has the same
    private int increasePower(ComponentState blockState) {
        if(blockState instanceof PortComponentState portComponentState) return portComponentState.getPower();
        return blockState instanceof WireComponentState wireComponentState ? wireComponentState.getPower() : 0;
    }
}
