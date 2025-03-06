package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class CrossoverComponent extends AbstractConductingComponent {
    private static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/crossover.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("textures/gui/newui/toolbox/icons/crossover.png");
    private static final Identifier TEXTURE_BRIDGE = IntegratedCircuit.id("textures/integrated_circuit/wire_bridge.png");

    public static final IntProperty POWER_X = IntProperty.of("power_x", 0, 15);
    public static final IntProperty POWER_Y = IntProperty.of("power_y", 0, 15);

    public CrossoverComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(POWER_X, 0).with(POWER_Y, 0));
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
        return Text.literal("─ ")
            .append(IntegratedCircuitScreen.getSignalStrengthText(state.get(POWER_X)))
            .append("   │ ")
            .append(IntegratedCircuitScreen.getSignalStrengthText(state.get(POWER_Y)));
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        int colorX = RedstoneWireBlock.getWireColor(state.get(POWER_X));
        int colorY = RedstoneWireBlock.getWireColor(state.get(POWER_Y));

        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_X, x, y, 0, ColorHelper.withAlpha(ColorHelper.channelFromFloat(a), colorX));
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_BRIDGE, x, y, 0, a);
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_Y, x, y, 0, ColorHelper.withAlpha(ColorHelper.channelFromFloat(a), colorY));
    }


    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (oldState.getComponent() == state.getComponent() || circuit.isClient) {
            return;
        }
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (circuit.isClient) return;
        update(circuit, pos, state);
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.getWeakRedstonePower(state, circuit, pos, direction);
    }


    @Override
    protected void update(Circuit circuit, ComponentPos pos, ComponentState state) {
        int powerX = getReceivedRedstonePower(circuit, pos, FlatDirection.Axis.X);
        int powerY = getReceivedRedstonePower(circuit, pos, FlatDirection.Axis.Y);
        if (state.get(POWER_X) != powerX || state.get(POWER_Y) != powerY) {
            if (circuit.getComponentState(pos) == state) {
                circuit.setComponentState(pos, state.with(POWER_X, powerX).with(POWER_Y, powerY), Component.NOTIFY_LISTENERS);
            }
            HashSet<ComponentPos> set = Sets.newHashSet();
            set.add(pos);
            for (FlatDirection direction : FlatDirection.VALUES) {
                set.add(pos.offset(direction));
            }
            for (ComponentPos blockPos : set) {
                circuit.updateNeighborsAlways(blockPos, this);
            }
        }
    }


    protected int getReceivedRedstonePower(Circuit circuit, ComponentPos pos, FlatDirection.Axis axis) {
        wiresGivePower = false;
        int i = 0;
        for (FlatDirection direction : FlatDirection.forAxis(axis)) {
            int power = circuit.getEmittedRedstonePower(pos.offset(direction), direction);
            if (power > i) i = power;
        }
        wiresGivePower = true;

        int j = 0;
        if (i < 15) {
            for (FlatDirection direction : FlatDirection.forAxis(axis)) {
                ComponentPos blockPos = pos.offset(direction);
                ComponentState blockState = circuit.getComponentState(blockPos);
                j = Math.max(j, blockState.increasePower(direction.getOpposite()));
            }
        }
        return Math.max(i, j - 1);
    }


    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (!wiresGivePower) {
            return 0;
        }
        return direction.getAxis() == FlatDirection.Axis.X ? state.get(POWER_X) : state.get(POWER_Y);
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return side.getAxis() == FlatDirection.Axis.X ? state.get(POWER_X) : state.get(POWER_Y);
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(POWER_X);
        builder.add(POWER_Y);
    }
}
