package net.replaceitem.integratedcircuit.circuit;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.components.FacingComponent;
import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Set;

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

    private ComponentState defaultState;

    private final Set<ComponentProperty<?>> properties;
    private final Byte2ObjectMap<ComponentState> stateMap = new Byte2ObjectOpenHashMap<>();

    public static final FlatDirection[] DIRECTIONS = new FlatDirection[]{FlatDirection.WEST, FlatDirection.EAST, FlatDirection.NORTH, FlatDirection.SOUTH};

    public Component(int id, Text name) {
        this.id = id;
        this.name = name;
        ComponentState.PropertyBuilder builder = new ComponentState.PropertyBuilder();
        this.appendProperties(builder);
        this.properties = builder.getProperties();
        createStateMap();
        this.defaultState = stateMap.get((byte) 0);
    }

    public int getId() {
        return id;
    }

    public Text getName() {
        return name;
    }
    public void appendProperties(ComponentState.PropertyBuilder builder) {

    }

    public Set<ComponentProperty<?>> getProperties() {
        return properties;
    }

    private void createStateMap() {
        for (int i = 0; i <= 0xFF; i++) {
            ComponentState state = new ComponentState(this, (byte) i);
            stateMap.put(state.encodeStateData(), state);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Component component && this.id == component.getId();
    }

    public ComponentState getDefaultPropertyState() {
        return getState((byte) 0);
    }

    public final ComponentState getState(byte data) {
        ComponentState componentState = this.stateMap.get(data);
        if(componentState == null) throw new RuntimeException("Invalid state data received: " + data);
        return componentState;
    }

    public void setDefaultState(ComponentState defaultState) {
        this.defaultState = defaultState;
    }

    public final ComponentState getDefaultState() {
        return this.defaultState;
    }
    
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState defaultState = this.getDefaultState();
        if(this.properties.contains(FacingComponent.FACING)) return defaultState.with(FacingComponent.FACING, rotation);
        return defaultState;
    }
    public abstract Identifier getItemTexture();
    public abstract void render(MatrixStack matrices, int x, int y, float a, ComponentState state);


    public static void replace(ComponentState state, ComponentState newState, Circuit world, ComponentPos pos, int flags) {
        replace(state, newState, world, pos, flags, 512);
    }

    public static void replace(ComponentState state, ComponentState newState, Circuit world, ComponentPos pos, int flags, int maxUpdateDepth) {
        if (newState != state) {
            if (newState.isAir()) {
                if(!world.isClient) {
                    world.breakBlock(pos, maxUpdateDepth);
                }
            } else {
                world.setComponentState(pos, newState, flags & ~SKIP_DROPS, maxUpdateDepth);
            }
        }
    }

    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        
    }

    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        
    }
    
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        
    }
    
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        
    }
    
    public void onPlaced(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        
    }

    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        
    }

    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return state;
    }

    public void prepare(ComponentState state, CircuitAccess circuit, ComponentPos pos, int flags, int maxUpdateDepth) {}

    public abstract boolean isSolidBlock(Circuit circuit, ComponentPos pos);

    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, FlatDirection direction) {
        return isSolidBlock(circuit, blockPos);
    }

    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return 0;
    }

    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return 0;
    }

    /**
     * Works slightly different from {@link net.minecraft.block.RedstoneWireBlock#increasePower(BlockState)},
     * in that it gets called from the component where the power is checked at, not where it's checked from.
     * This is done so different behaviours can be used for wire/port/crossover, without several checks.
     */
    public int increasePower(ComponentState state, FlatDirection side) {
        return 0;
    };

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

    public Text getHoverInfoText(ComponentState state) {
        return Text.empty();
    }
}
