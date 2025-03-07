package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class RepeaterComponent extends AbstractRedstoneGateComponent {
    private static final Identifier ITEM_TEXTURE = Identifier.ofVanilla("textures/item/repeater.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/repeater");
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/repeater_off.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/repeater_on.png");
    private static final Identifier TEXTURE_TORCH_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_top_off.png");
    private static final Identifier TEXTURE_TORCH_ON = IntegratedCircuit.id("textures/integrated_circuit/torch_top_on.png");
    private static final Identifier TEXTURE_BAR = IntegratedCircuit.id("textures/integrated_circuit/repeater_bar.png");

    public static final IntProperty DELAY = Properties.DELAY;
    public static final BooleanProperty LOCKED = Properties.LOCKED;

    public RepeaterComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, FlatDirection.NORTH).with(POWERED, false).with(DELAY, 1).with(LOCKED, false));
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return TOOL_TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(POWERED) ? 15 : 0);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        FlatDirection renderedRotation = state.get(FACING).getOpposite();

        Identifier baseTexture = state.get(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, baseTexture, x, y, renderedRotation.getIndex(), a);


        Identifier torchTexture = state.get(POWERED) ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        
        IntegratedCircuitScreen.renderPartialTexture(drawContext, torchTexture, x, y, 6, 1, 4, 4, renderedRotation.getIndex(), a);

        boolean locked = state.get(LOCKED);
        Identifier knobTexture = locked ? TEXTURE_BAR : torchTexture;
        int knobOffsetAmount = (state.get(DELAY)-1) * 2;
        if(locked) {
            IntegratedCircuitScreen.renderPartialTexture(drawContext, knobTexture, x, y, 2, 6 + knobOffsetAmount, 12, 2, renderedRotation.getIndex(), a);
        } else {
            IntegratedCircuitScreen.renderPartialTexture(drawContext, knobTexture, x, y, 6, 5 + knobOffsetAmount, 4, 4, renderedRotation.getIndex(), a);
        }
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        circuit.setComponentState(pos, state.cycle(DELAY), Block.NOTIFY_ALL);
    }

    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return state.get(DELAY) * 2;
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState state = super.getPlacementState(circuit, pos, rotation);
        return state.with(LOCKED, this.isLocked(circuit, pos, state));
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (!circuit.isClient && direction.getAxis() != state.get(FACING).getAxis()) {
            return state.with(LOCKED, this.isLocked(circuit, pos, state));
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
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(DELAY, LOCKED);
    }
}
