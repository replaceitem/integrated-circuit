package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import org.jetbrains.annotations.Nullable;

public class CopperBulbComponent extends Component {
    private static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/copper_bulb");
    private static final Identifier TEXTURE_LIT = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_lit.png");
    private static final Identifier TEXTURE_POWERED = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_powered.png");
    private static final Identifier TEXTURE_LIT_POWERED = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_lit_powered.png");

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CopperBulbComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(LIT, false).setValue(POWERED, false));
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return TOOL_TEXTURE;
    }

    private Identifier getTexture(boolean lit, boolean powered) {
        return lit ? (powered ? TEXTURE_LIT_POWERED : TEXTURE_LIT) : (powered ? TEXTURE_POWERED : ITEM_TEXTURE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, getTexture(state.getValue(LIT), state.getValue(POWERED)), x, y, 0, a);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (oldState.getComponent() != state.getComponent() && circuit instanceof ServerCircuit serverCircuit) {
            update(state, serverCircuit, pos);
        }
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (circuit instanceof ServerCircuit serverCircuit) {
            update(state, serverCircuit, pos);
        }
    }

    public void update(ComponentState state, ServerCircuit circuit, ComponentPos pos) {
        boolean receivingPower = circuit.isReceivingRedstonePower(pos);
        if (receivingPower != state.getValue(POWERED)) {
            ComponentState newState = state;
            if (receivingPower) {
                newState = newState.cycle(LIT);
                circuit.playSound(null, newState.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 1, 1);
            }

            circuit.setComponentState(pos, newState.setValue(POWERED, receivingPower), NOTIFY_ALL);
        }
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return true;
    }

    @Override
    public boolean hasComparatorOutput(ComponentState componentState) {
        return true;
    }

    @Override
    public int getComparatorOutput(ComponentState state, Circuit circuit, ComponentPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(LIT, POWERED);
    }
}
