package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
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

public class ButtonComponent extends FacingComponent {
    private static final Identifier ITEM_TEXTURE_STONE = IntegratedCircuit.id("textures/integrated_circuit/button_stone.png");
    private static final Identifier ITEM_TEXTURE_WOOD = IntegratedCircuit.id("textures/integrated_circuit/button_wood.png");
    private static final Identifier TOOL_TEXTURE_STONE = IntegratedCircuit.id("toolbox/icons/button_stone");
    private static final Identifier TOOL_TEXTURE_WOOD = IntegratedCircuit.id("toolbox/icons/button_wood");

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final boolean wooden;

    public ButtonComponent(Settings settings, boolean wooden) {
        super(settings);
        this.wooden = wooden;
        this.setDefaultState(this.getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return wooden ? ITEM_TEXTURE_WOOD : ITEM_TEXTURE_STONE;
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return wooden ? TOOL_TEXTURE_WOOD : TOOL_TEXTURE_STONE;
    }

    @Override
    public net.minecraft.network.chat.Component getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.getValue(POWERED) ? 15 : 0);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        Identifier texture = getItemTexture();
        if(texture != null) {
            float b = state.getValue(POWERED) ? 0.5f : 1f;
            IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, texture, x, y, state.getValue(FACING).getIndex(), ARGB.colorFromFloat(a, b, b, b));
        }
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        if(state.getValue(POWERED)) return;
        powerOn(state, circuit, pos);
        this.playClickSound(player, circuit, true);
    }

    protected void playClickSound(@Nullable Player player, Circuit circuit, boolean powered) {
        circuit.playSound(powered ? player : null, this.getClickSound(powered), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    protected SoundEvent getClickSound(boolean powered) {
        return powered ?
                (this.wooden ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_ON)
                :(this.wooden ? SoundEvents.WOODEN_BUTTON_CLICK_OFF : SoundEvents.STONE_BUTTON_CLICK_OFF);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if(state.isOf(newState.getComponent())) return;
        if(state.getValue(POWERED)) {
            circuit.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, RandomSource random) {
        if(!state.getValue(POWERED)) return;
        circuit.setComponentState(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        circuit.updateNeighborsAlways(pos, this);
        this.playClickSound(null, circuit, false);
    }

    public void powerOn(ComponentState state, Circuit circuit, ComponentPos pos) {
        circuit.setComponentState(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
        circuit.updateNeighborsAlways(pos, this);
        circuit.scheduleBlockTick(pos, this, wooden ? 30 : 20);
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
