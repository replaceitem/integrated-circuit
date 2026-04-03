package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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
    private static final Identifier ITEM_TEXTURE = Identifier.withDefaultNamespace("textures/item/redstone.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/redstone");
    private static final Identifier TEXTURE_DOT = IntegratedCircuit.id("textures/integrated_circuit/wire_dot.png");

    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
    
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

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
        this.setDefaultState(this.getStateDefinition().any()
                .setValue(CONNECTED_NORTH, false)
                .setValue(CONNECTED_EAST, false)
                .setValue(CONNECTED_SOUTH, false)
                .setValue(CONNECTED_WEST, false)
                .setValue(POWER, 0)
        );
        dotState = this.getDefaultState()
                .setValue(CONNECTED_NORTH, true)
                .setValue(CONNECTED_EAST, true)
                .setValue(CONNECTED_SOUTH, true)
                .setValue(CONNECTED_WEST, true)
                .setValue(POWER, 0);
    }
    
    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        return this.getPlacementState(circuit, dotState, pos);
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return TOOL_TEXTURE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        final int size = IntegratedCircuitScreen.COMPONENT_SIZE;
        final int halfSize = size/2;

        int color = ARGB.color(ARGB.as8BitChannel(a), RedStoneWireBlock.getColorForPower(state.getValue(POWER)));

        if(state.getValue(CONNECTED_NORTH)) IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_Y, x, y, 0, color, 0, 0, size, halfSize);
        if(state.getValue(CONNECTED_EAST)) IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_X, x, y, 0, color, halfSize, 0, halfSize, size);
        if(state.getValue(CONNECTED_SOUTH)) IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_Y, x, y, 0, color, 0, halfSize, size, halfSize);
        if(state.getValue(CONNECTED_WEST)) IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_X, x, y, 0, color, 0, 0, halfSize, size);

        int connections = 0;
        for (FlatDirection direction : FlatDirection.VALUES) if(state.getValue(DIRECTION_TO_CONNECTION_PROPERTY.get(direction))) connections++;
        if(connections != 2) IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_DOT, x, y, 0, color, 0, 0, size, size);
        
        if(!(state.getValue(CONNECTED_NORTH) && state.getValue(CONNECTED_SOUTH) || state.getValue(CONNECTED_EAST) && state.getValue(CONNECTED_WEST))) {
            IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_DOT, x, y, 0, color, 0, 0, size, size);
        }
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        boolean wireConnection = this.getRenderConnectionType(circuit, pos, direction);
        if (wireConnection == state.getValue(DIRECTION_TO_CONNECTION_PROPERTY.get(direction)) && !isFullyConnected(state)) {
            return state.setValue(DIRECTION_TO_CONNECTION_PROPERTY.get(direction), wireConnection);
        }
        return this.getPlacementState(circuit, dotState.setValue(POWER, state.getValue(POWER)).setValue(DIRECTION_TO_CONNECTION_PROPERTY.get(direction), wireConnection), pos);
    }


    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        if (isFullyConnected(state) || isNotConnected(state)) {
            ComponentState newState = isFullyConnected(state) ? this.getDefaultState() : dotState;
            newState = newState.setValue(POWER, state.getValue(POWER));
            if ((newState = this.getPlacementState(circuit, newState, pos)) != state) {
                circuit.setComponentState(pos, newState, Block.UPDATE_ALL);
                this.updateForNewState(circuit, pos, state, newState);
            }
        }
    }

    private void updateForNewState(Circuit world, ComponentPos pos, ComponentState oldState, ComponentState newState) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            ComponentPos blockPos = pos.offset(direction);
            BooleanProperty connectionProperty = DIRECTION_TO_CONNECTION_PROPERTY.get(direction);
            if (oldState.getValue(connectionProperty) == newState.getValue(connectionProperty) || !world.getComponentState(blockPos).isSolidBlock(world, blockPos)) continue;
            world.updateNeighborsExcept(blockPos, newState.getComponent(), direction.getOpposite());
        }
    }

    private ComponentState getPlacementState(Circuit circuit, ComponentState state, ComponentPos pos) {
        boolean notConnected = isNotConnected(state);
        state = this.getDefaultWireState(circuit, getDefaultState().setValue(POWER, state.getValue(POWER)), pos);
        if (notConnected && isNotConnected(state)) {
            return state;
        }
        boolean n = state.getValue(CONNECTED_NORTH);
        boolean s = state.getValue(CONNECTED_SOUTH);
        boolean e = state.getValue(CONNECTED_EAST);
        boolean w = state.getValue(CONNECTED_WEST);
        boolean ns = !n && !s;
        boolean ew = !e && !w;
        if (!w && ns) {
            state = state.setValue(CONNECTED_WEST, true);
        }
        if (!e && ns) {
            state = state.setValue(CONNECTED_EAST, true);
        }
        if (!n && ew) {
            state = state.setValue(CONNECTED_NORTH, true);
        }
        if (!s && ew) {
            state = state.setValue(CONNECTED_SOUTH, true);
        }
        return state;
    }
    
    private ComponentState getDefaultWireState(Circuit circuit, ComponentState state, ComponentPos pos) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            BooleanProperty connectionProperty = DIRECTION_TO_CONNECTION_PROPERTY.get(direction);
            if (state.getValue(connectionProperty)) continue;
            boolean wireConnection = this.getRenderConnectionType(circuit, pos, direction);
            state = state.setValue(connectionProperty, wireConnection);
        }
        return state;
    }

    private static boolean isNotConnected(ComponentState state) {
        return !(
                state.getValue(CONNECTED_NORTH)
                || state.getValue(CONNECTED_EAST)
                || state.getValue(CONNECTED_SOUTH)
                || state.getValue(CONNECTED_WEST)
        );
    }

    private static boolean isFullyConnected(ComponentState state) {
        return state.getValue(CONNECTED_NORTH)
                        && state.getValue(CONNECTED_EAST)
                        && state.getValue(CONNECTED_SOUTH)
                        && state.getValue(CONNECTED_WEST);
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
            FlatDirection rotation = state.getValue(FacingComponent.FACING);
            return rotation == direction || rotation.getOpposite() == direction;
        }
        if (state.isOf(Components.OBSERVER)) {
            return direction == state.getValue(FacingComponent.FACING);
        }
        return state.emitsRedstonePower();
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (!wiresGivePower) {
            return 0;
        }
        int i = state.getValue(POWER);
        if (i == 0) {
            return 0;
        }
        if (this.getPlacementState(circuit, state, pos).getValue(DIRECTION_TO_CONNECTION_PROPERTY.get(direction.getOpposite()))) {
            return i;
        }
        return 0;
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return state.getValue(POWER);
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTED_NORTH, CONNECTED_EAST, CONNECTED_SOUTH, CONNECTED_WEST);
        builder.add(POWER);
    }
}
