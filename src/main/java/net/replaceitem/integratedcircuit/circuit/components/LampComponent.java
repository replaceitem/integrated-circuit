package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class LampComponent extends Component {
    public static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/lamp.png");
    public static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("textures/gui/newui/toolbox/icons/lamp.png");
    public static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/lamp_on.png");

    public static final BooleanProperty LIT = Properties.LIT;

    public LampComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(LIT, false));
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
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        Identifier texture = state.get(LIT) ? TEXTURE_ON : ITEM_TEXTURE;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, texture, x, y, 0, a);
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(LIT) ? 15 : 0);
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        return this.getDefaultState().with(LIT, circuit.isReceivingRedstonePower(pos));
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (!circuit.isClient) {
            boolean bl = state.get(LIT);
            if (bl != circuit.isReceivingRedstonePower(pos)) {
                if (bl) {
                    circuit.scheduleBlockTick(pos, this, 4);
                } else {
                    circuit.setComponentState(pos, state.cycle(LIT), Component.NOTIFY_LISTENERS);
                }
            }

        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if (state.get(LIT) && !circuit.isReceivingRedstonePower(pos)) {
            circuit.setComponentState(pos, state.cycle(LIT), Component.NOTIFY_LISTENERS);
        }
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return true;
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }
}
