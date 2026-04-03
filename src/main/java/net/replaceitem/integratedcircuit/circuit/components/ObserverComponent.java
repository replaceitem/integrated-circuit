package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

public class ObserverComponent extends FacingComponent {
    public static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/observer.png");
    public static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/observer");
    public static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/observer_on.png");

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ObserverComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(FACING, FlatDirection.NORTH).setValue(POWERED, false));
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
    public net.minecraft.network.chat.Component getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.getValue(POWERED) ? 15 : 0);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, state.getValue(POWERED) ? TEXTURE_ON : ITEM_TEXTURE, x, y, state.getValue(FACING).getOpposite().getIndex(), a);
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            circuit.setComponentState(pos, state.setValue(POWERED, false), Block.UPDATE_CLIENTS);
        } else {
            circuit.setComponentState(pos, state.setValue(POWERED, true), Block.UPDATE_CLIENTS);
            circuit.scheduleBlockTick(pos, this, 2);
        }
        this.updateNeighbors(circuit, pos, state);
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (state.getValue(FACING) == direction && !state.getValue(POWERED)) {
            this.scheduleTick(circuit, pos);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    private void scheduleTick(Circuit circuit, ComponentPos pos) {
        if (!circuit.isClient && !circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            circuit.scheduleBlockTick(pos, this, 2);
        }
    }

    protected void updateNeighbors(Circuit world, ComponentPos pos, ComponentState state) {
        FlatDirection direction = state.getValue(FACING);
        ComponentPos blockPos = pos.offset(direction.getOpposite());
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.getWeakRedstonePower(circuit, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (state.getValue(POWERED) && state.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (state.isOf(oldState.getComponent())) {
            return;
        }
        if(!circuit.isClient && state.getValue(POWERED) && !circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            ComponentState blockState = state.setValue(POWERED, false);
            circuit.setComponentState(pos, blockState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            this.updateNeighbors(circuit, pos, blockState);
        }
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if (state.isOf(newState.getComponent())) {
            return;
        }
        if (!circuit.isClient && state.getValue(POWERED) && circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            this.updateNeighbors(circuit, pos, state.setValue(POWERED, false));
        }
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, FlatDirection direction) {
        return true;
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }
}
