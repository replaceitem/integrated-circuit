package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.CircuitAccess;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ObserverComponentState;
import net.replaceitem.integratedcircuit.circuit.state.RepeaterComponentState;
import net.replaceitem.integratedcircuit.circuit.state.WireComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class WireComponent extends AbstractWireComponent {
    public WireComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.wire"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/item/redstone.png");
    private static final Identifier TEXTURE_DOT = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_dot.png");
    
    @Override
    public ComponentState getDefaultState() {
        return new WireComponentState((byte) 0b0000, (byte) 0);
    }

    // yes, this name is confusing, since it returns the cross state, but for some reason RedstoneWireBlock.dotState (yarn) seems to be the same
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

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof WireComponentState wireComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (!wiresGivePower) {
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
}
