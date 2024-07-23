package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jetbrains.annotations.Nullable;

public class CopperBulbComponent extends Component {
    
    public static final BooleanProperty LIT = Properties.LIT;
    public static final BooleanProperty POWERED = Properties.POWERED;
    
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb.png");
    private static final Identifier TEXTURE_LIT = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_lit.png");
    private static final Identifier TEXTURE_POWERED = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_powered.png");
    private static final Identifier TEXTURE_LIT_POWERED = IntegratedCircuit.id("textures/integrated_circuit/copper_bulb_lit_powered.png");
    
    public CopperBulbComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(LIT, false).with(POWERED, false));
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return TEXTURE;
    }
    
    private Identifier getTexture(boolean lit, boolean powered) {
        return lit ? (powered ? TEXTURE_LIT_POWERED : TEXTURE_LIT) : (powered ? TEXTURE_POWERED : TEXTURE);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.renderComponentTexture(drawContext, getTexture(state.get(LIT), state.get(POWERED)), x, y, 0, 1, 1, 1, a);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (oldState.getComponent() != state.getComponent() && circuit instanceof ServerCircuit serverCircuit) {
            update(state, serverCircuit, pos);
        }
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if(circuit instanceof ServerCircuit serverCircuit) {
            update(state, serverCircuit, pos);
        }
    }

    public void update(ComponentState state, ServerCircuit circuit, ComponentPos pos) {
        boolean receivingPower = circuit.isReceivingRedstonePower(pos);
        if (receivingPower != state.get(POWERED)) {
            ComponentState newState = state;
            if (receivingPower) {
                newState = newState.cycle(LIT);
                circuit.playSound(null, newState.get(LIT) ? SoundEvents.BLOCK_COPPER_BULB_TURN_ON : SoundEvents.BLOCK_COPPER_BULB_TURN_OFF, SoundCategory.BLOCKS, 1, 1);
            }

            circuit.setComponentState(pos, newState.with(POWERED, receivingPower), NOTIFY_ALL);
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
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(LIT, POWERED);
    }
}
