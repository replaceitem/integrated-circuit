package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class LeverComponent extends FacingComponent {
    private static final Identifier ITEM_TEXTURE = Identifier.withDefaultNamespace("textures/block/lever.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/lever");
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/lever_off.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/lever_on.png");

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LeverComponent(Settings settings) {
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
        Identifier texture = state.getValue(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, texture, x, y, state.getValue(FACING).getIndex(), a);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        if(circuit.isClient) {
            return;
        }
        state = this.togglePower(state, circuit, pos);
        float f = state.getValue(POWERED) ? 0.6f : 0.5f;
        circuit.playSound(null, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1, f);
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
        if(state.getValue(POWERED)) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.getValue(POWERED) ? 15 : 0;
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
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }
}
