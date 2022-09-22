package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.RotatableComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.Direction;

public abstract class Component {
    
    // copy from Block, only few are needed, but all kept for future
    public static final int NOTIFY_NEIGHBORS = 1;
    public static final int NOTIFY_LISTENERS = 2;
    public static final int NO_REDRAW = 4;
    public static final int REDRAW_ON_MAIN_THREAD = 8;
    public static final int FORCE_STATE = 16;
    public static final int SKIP_DROPS = 32;
    public static final int MOVED = 64;
    public static final int SKIP_LIGHTING_UPDATES = 128;

    public static final int NOTIFY_ALL = 3;
    
    private final int id;
    private final Text name;


    public static final Direction[] DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

    public Component(int id, Text name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public Text getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Component component && this.id == component.getId();
    }

    public ComponentState getState(byte data) {
        return getDefaultState();
    }

    public abstract ComponentState getDefaultState();
    public ComponentState getPlacementState(ServerCircuit circuit, ComponentPos pos, Direction rotation) {
        ComponentState defaultState = getDefaultState();
        if(defaultState instanceof RotatableComponentState rotatableComponentState) rotatableComponentState.setRotation(rotation);
        return defaultState;
    }
    public abstract Identifier getItemTexture();
    public abstract void render(MatrixStack matrices, int x, int y, float a, ComponentState state);


    public static void replace(ComponentState state, ComponentState newState, Circuit world, ComponentPos pos, int flags) {
        if (!newState.equals(state)) { // IN MC THIS IS == (but something I can't seem to track down makes == not work, leading to a StackOverflowError, lets home this doesn't haunt me later ...)
            if (newState.isAir()) {
                world.breakBlock(pos);
            } else {
                world.setComponentState(pos, newState, flags & ~SKIP_DROPS);
            }
        }
    }

    public void neighborUpdate(ComponentState state, ServerCircuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        
    }

    public void onBlockAdded(ComponentState state, ServerCircuit circuit, ComponentPos pos, ComponentState oldState) {
        
    }
    
    public void onStateReplaced(ComponentState state, ServerCircuit circuit, ComponentPos pos, ComponentState newState) {
        
    }
    
    public void onUse(ComponentState state, ServerCircuit circuit, ComponentPos pos) {
        
    }
    
    public void onPlaced(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        
    }

    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        
    }

    public ComponentState getStateForNeighborUpdate(ComponentState state, Direction direction, ComponentState neighborState, ServerCircuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return state;
    }

    public void prepare(ComponentState state, ServerCircuit circuit, ComponentPos pos, int flags) {}

    public abstract boolean isSolidBlock(Circuit circuit, ComponentPos pos);

    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, Direction direction) {
        return isSolidBlock(circuit, blockPos);
    }

    public int getWeakRedstonePower(ComponentState state, ServerCircuit circuit, ComponentPos pos, Direction direction) {
        return 0;
    }

    public int getStrongRedstonePower(ComponentState state, ServerCircuit circuit, ComponentPos pos, Direction direction) {
        return 0;
    }

    public boolean canPlaceAt(ComponentState state, Circuit circuit, ComponentPos pos) {
        return true;
    }


    @Override
    public String toString() {
        return this.name.getString();
    }

    public boolean emitsRedstonePower(ComponentState state) {
        return false;
    }
}
