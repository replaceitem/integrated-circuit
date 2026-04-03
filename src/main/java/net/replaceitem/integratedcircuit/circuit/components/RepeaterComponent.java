package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class RepeaterComponent extends AbstractRedstoneGateComponent {
    private static final Identifier ITEM_TEXTURE = Identifier.withDefaultNamespace("textures/item/repeater.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/repeater");
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/repeater_off.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/repeater_on.png");
    private static final Identifier TEXTURE_TORCH_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_top_off.png");
    private static final Identifier TEXTURE_TORCH_ON = IntegratedCircuit.id("textures/integrated_circuit/torch_top_on.png");
    private static final Identifier TEXTURE_BAR = IntegratedCircuit.id("textures/integrated_circuit/repeater_bar.png");

    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    public RepeaterComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(FACING, FlatDirection.NORTH).setValue(POWERED, false).setValue(DELAY, 1).setValue(LOCKED, false));
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
        FlatDirection renderedRotation = state.getValue(FACING).getOpposite();

        Identifier baseTexture = state.getValue(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, baseTexture, x, y, renderedRotation.getIndex(), a);


        Identifier torchTexture = state.getValue(POWERED) ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        
        IntegratedCircuitScreen.extractPartialTextureRenderState(drawContext, torchTexture, x, y, 6, 1, 4, 4, renderedRotation.getIndex(), a);

        boolean locked = state.getValue(LOCKED);
        Identifier knobTexture = locked ? TEXTURE_BAR : torchTexture;
        int knobOffsetAmount = (state.getValue(DELAY)-1) * 2;
        if(locked) {
            IntegratedCircuitScreen.extractPartialTextureRenderState(drawContext, knobTexture, x, y, 2, 6 + knobOffsetAmount, 12, 2, renderedRotation.getIndex(), a);
        } else {
            IntegratedCircuitScreen.extractPartialTextureRenderState(drawContext, knobTexture, x, y, 6, 5 + knobOffsetAmount, 4, 4, renderedRotation.getIndex(), a);
        }
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        circuit.setComponentState(pos, state.cycle(DELAY), Block.UPDATE_ALL);
    }

    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return state.getValue(DELAY) * 2;
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState state = super.getPlacementState(circuit, pos, rotation);
        return state.setValue(LOCKED, this.isLocked(circuit, pos, state));
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (!circuit.isClient && direction.getAxis() != state.getValue(FACING).getAxis()) {
            return state.setValue(LOCKED, this.isLocked(circuit, pos, state));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    @Override
    public boolean isLocked(Circuit circuit, ComponentPos pos, ComponentState state) {
        return this.getMaxInputLevelSides(circuit, pos, state) > 0;
    }

    @Override
    protected boolean isValidInput(ComponentState state) {
        return isRedstoneGate(state);
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(DELAY, LOCKED);
    }
}
