package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WireComponent extends AbstractWireComponent {

    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.of("connected_north");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.of("connected_east");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.of("connected_south");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.of("connected_west");
    
    public static final IntProperty POWER = Properties.POWER;


    public static final Map<FlatDirection, BooleanProperty> DIRECTION_TO_CONNECTION_PROPERTY = Maps.newEnumMap(ImmutableMap.of(
            FlatDirection.NORTH, CONNECTED_NORTH,
            FlatDirection.EAST, CONNECTED_EAST,
            FlatDirection.SOUTH, CONNECTED_SOUTH,
            FlatDirection.WEST, CONNECTED_WEST
    ));

    // yes, this name is confusing, since it returns the cross state, but for some reason RedstoneWireBlock.dotState (yarn) seems to be the same
    private final ComponentState dotState;

    public WireComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(CONNECTED_NORTH, false)
                .with(CONNECTED_EAST, false)
                .with(CONNECTED_SOUTH, false)
                .with(CONNECTED_WEST, false)
                .with(POWER, 0)
        );
        dotState = this.getDefaultState()
                .with(CONNECTED_NORTH, true)
                .with(CONNECTED_EAST, true)
                .with(CONNECTED_SOUTH, true)
                .with(CONNECTED_WEST, true)
                .with(POWER, 0);
    }

    private static final Identifier ITEM_TEXTURE = Identifier.ofVanilla("textures/item/redstone.png");
    private static final Identifier TEXTURE_DOT = IntegratedCircuit.id("textures/integrated_circuit/wire_dot.png");
    
    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        return this.getPlacementState(circuit, dotState, pos);
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        final int size = IntegratedCircuitScreen.COMPONENT_SIZE;
        final int halfSize = size/2;

        int color = ColorHelper.withAlpha(ColorHelper.channelFromFloat(a), RedstoneWireBlock.getWireColor(state.get(POWER)));

        if(state.get(CONNECTED_NORTH)) IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_Y, x, y, 0, color, 0, 0, size, halfSize);
        if(state.get(CONNECTED_EAST)) IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_X, x, y, 0, color, halfSize, 0, halfSize, size);
        if(state.get(CONNECTED_SOUTH)) IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_Y, x, y, 0, color, 0, halfSize, size, halfSize);
        if(state.get(CONNECTED_WEST)) IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_X, x, y, 0, color, 0, 0, halfSize, size);

        int connections = 0;
        for (FlatDirection direction : FlatDirection.VALUES) if(state.get(DIRECTION_TO_CONNECTION_PROPERTY.get(direction))) connections++;
        if(connections != 2) IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_DOT, x, y, 0, color, 0, 0, size, size);
        
        if(!(state.get(CONNECTED_NORTH) && state.get(CONNECTED_SOUTH) || state.get(CONNECTED_EAST) && state.get(CONNECTED_WEST))) {
            IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_DOT, x, y, 0, color, 0, 0, size, size);
        }
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        boolean wireConnection = this.getRenderConnectionType(circuit, pos, direction);
        if (wireConnection == state.get(DIRECTION_TO_CONNECTION_PROPERTY.get(direction)) && !isFullyConnected(state)) {
            return state.with(DIRECTION_TO_CONNECTION_PROPERTY.get(direction), wireConnection);
        }
        return this.getPlacementState(circuit, dotState.with(POWER, state.get(POWER)).with(DIRECTION_TO_CONNECTION_PROPERTY.get(direction), wireConnection), pos);
    }


    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        if (isFullyConnected(state) || isNotConnected(state)) {
            ComponentState newState = isFullyConnected(state) ? this.getDefaultState() : dotState;
            newState = newState.with(POWER, state.get(POWER));
            if ((newState = this.getPlacementState(circuit, newState, pos)) != state) {
                circuit.setComponentState(pos, newState, Block.NOTIFY_ALL);
                this.updateForNewState(circuit, pos, state, newState);
            }
        }
    }

    private void updateForNewState(Circuit world, ComponentPos pos, ComponentState oldState, ComponentState newState) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            ComponentPos blockPos = pos.offset(direction);
            BooleanProperty connectionProperty = DIRECTION_TO_CONNECTION_PROPERTY.get(direction);
            if (oldState.get(connectionProperty) == newState.get(connectionProperty) || !world.getComponentState(blockPos).isSolidBlock(world, blockPos)) continue;
            world.updateNeighborsExcept(blockPos, newState.getComponent(), direction.getOpposite());
        }
    }

    private ComponentState getPlacementState(Circuit circuit, ComponentState state, ComponentPos pos) {
        boolean notConnected = isNotConnected(state);
        state = this.getDefaultWireState(circuit, getDefaultState().with(POWER, state.get(POWER)), pos);
        if (notConnected && isNotConnected(state)) {
            return state;
        }
        boolean n = state.get(CONNECTED_NORTH);
        boolean s = state.get(CONNECTED_SOUTH);
        boolean e = state.get(CONNECTED_EAST);
        boolean w = state.get(CONNECTED_WEST);
        boolean ns = !n && !s;
        boolean ew = !e && !w;
        if (!w && ns) {
            state = state.with(CONNECTED_WEST, true);
        }
        if (!e && ns) {
            state = state.with(CONNECTED_EAST, true);
        }
        if (!n && ew) {
            state = state.with(CONNECTED_NORTH, true);
        }
        if (!s && ew) {
            state = state.with(CONNECTED_SOUTH, true);
        }
        return state;
    }
    
    private ComponentState getDefaultWireState(Circuit circuit, ComponentState state, ComponentPos pos) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            BooleanProperty connectionProperty = DIRECTION_TO_CONNECTION_PROPERTY.get(direction);
            if (state.get(connectionProperty)) continue;
            boolean wireConnection = this.getRenderConnectionType(circuit, pos, direction);
            state = state.with(connectionProperty, wireConnection);
        }
        return state;
    }

    private static boolean isNotConnected(ComponentState state) {
        return !(
                state.get(CONNECTED_NORTH)
                || state.get(CONNECTED_EAST)
                || state.get(CONNECTED_SOUTH)
                || state.get(CONNECTED_WEST)
        );
    }

    private static boolean isFullyConnected(ComponentState state) {
        return state.get(CONNECTED_NORTH)
                        && state.get(CONNECTED_EAST)
                        && state.get(CONNECTED_SOUTH)
                        && state.get(CONNECTED_WEST);
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
        if (state.isOf(Components.REPEATER)) {
            FlatDirection rotation = state.get(FacingComponent.FACING);
            return rotation == direction || rotation.getOpposite() == direction;
        }
        if (state.isOf(Components.OBSERVER)) {
            return direction == state.get(FacingComponent.FACING);
        }
        return state.emitsRedstonePower() && direction != null;
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (!wiresGivePower) {
            return 0;
        }
        int i = state.get(POWER);
        if (i == 0) {
            return 0;
        }
        if (this.getPlacementState(circuit, state, pos).get(DIRECTION_TO_CONNECTION_PROPERTY.get(direction.getOpposite()))) {
            return i;
        }
        return 0;
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return state.get(POWER);
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST);
        builder.add(POWER);
    }
}
