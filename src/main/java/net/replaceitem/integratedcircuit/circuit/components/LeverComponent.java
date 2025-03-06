package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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

public class LeverComponent extends FacingComponent {
    private static final Identifier ITEM_TEXTURE = Identifier.ofVanilla("textures/block/lever.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("textures/gui/newui/toolbox/icons/lever.png");
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/lever_off.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/lever_on.png");

    public static final BooleanProperty POWERED = Properties.POWERED;

    public LeverComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, FlatDirection.NORTH).with(POWERED, false));
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
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(POWERED) ? 15 : 0);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        Identifier texture = state.get(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, texture, x, y, state.get(FACING).getIndex(), a);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        if(circuit.isClient) {
            return;
        }
        state = this.togglePower(state, circuit, pos);
        float f = state.get(POWERED) ? 0.6f : 0.5f;
        circuit.playSound(null, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1, f);
    }

    public ComponentState togglePower(ComponentState state, Circuit circuit, ComponentPos pos) {
        state = state.cycle(POWERED);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        return state;
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if(state.isOf(newState.getComponent())) return;
        if(state.get(POWERED)) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }
}
