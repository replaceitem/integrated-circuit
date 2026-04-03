package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

public class CrossoverComponent extends AbstractConductingComponent {
    private static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/crossover.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/crossover");
    private static final Identifier TEXTURE_BRIDGE = IntegratedCircuit.id("textures/integrated_circuit/wire_bridge.png");

    public static final IntegerProperty POWER_X = IntegerProperty.create("power_x", 0, 15);
    public static final IntegerProperty POWER_Y = IntegerProperty.create("power_y", 0, 15);

    public CrossoverComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(POWER_X, 0).setValue(POWER_Y, 0));
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
        return net.minecraft.network.chat.Component.literal("─ ")
            .append(IntegratedCircuitScreen.getSignalStrengthText(state.getValue(POWER_X)))
            .append(" │ ")
            .append(IntegratedCircuitScreen.getSignalStrengthText(state.getValue(POWER_Y)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        int colorX = RedStoneWireBlock.getColorForPower(state.getValue(POWER_X));
        int colorY = RedStoneWireBlock.getColorForPower(state.getValue(POWER_Y));

        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_X, x, y, 0, ARGB.color(ARGB.as8BitChannel(a), colorX));
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_BRIDGE, x, y, 0, a);
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, TEXTURE_Y, x, y, 0, ARGB.color(ARGB.as8BitChannel(a), colorY));
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
        if (state.getValue(POWER_X) != powerX || state.getValue(POWER_Y) != powerY) {
            if (circuit.getComponentState(pos) == state) {
                circuit.setComponentState(pos, state.setValue(POWER_X, powerX).setValue(POWER_Y, powerY), Component.NOTIFY_LISTENERS);
            }
            this.updateAfterSignalStrengthChange(circuit, pos);
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
        return direction.getAxis() == FlatDirection.Axis.X ? state.getValue(POWER_X) : state.getValue(POWER_Y);
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return side.getAxis() == FlatDirection.Axis.X ? state.getValue(POWER_X) : state.getValue(POWER_Y);
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(POWER_X);
        builder.add(POWER_Y);
    }
}
