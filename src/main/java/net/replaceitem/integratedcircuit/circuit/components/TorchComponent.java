package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.*;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class TorchComponent extends FacingComponent {
    private static final Identifier ITEM_TEXTURE = Identifier.ofVanilla("textures/block/redstone_torch.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("textures/gui/newui/toolbox/icons/torch.png");
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/torch.png");
    private static final Identifier TEXTURE_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_off.png");

    public static final BooleanProperty LIT = Properties.LIT;

    private static final Map<ServerCircuit, List<BurnoutEntry>> BURNOUT_MAP = new WeakHashMap<>();

    public TorchComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(LIT, true));
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState componentState = this.getDefaultState().with(FACING, rotation);
        if (!componentState.canPlaceAt(circuit, pos)) {
            for (FlatDirection value : CircuitNeighborUpdater.UPDATE_ORDER) {
                componentState = componentState.with(FACING, value);
                if (componentState.canPlaceAt(circuit, pos)) return componentState;
            }
            return null;
        }
        return componentState;
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
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(LIT) ? 15 : 0);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        Identifier texture = state.get(LIT) ? TEXTURE : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, texture, x, y, state.get(FACING).getIndex(), a);
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (direction.getOpposite() == state.get(FACING) && !canPlaceAt(state, circuit, pos)) {
            return Components.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public boolean canPlaceAt(ComponentState state, Circuit circuit, ComponentPos pos) {
        FlatDirection direction = state.get(FACING);
        ComponentPos blockPos = pos.offset(direction.getOpposite());
        ComponentState blockState = circuit.getComponentState(blockPos);
        return blockState.getComponent().isSideSolidFullSquare(circuit, blockPos, direction);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        boolean shouldUnpower = this.shouldUnpower(circuit, pos, state);
        List<BurnoutEntry> list = BURNOUT_MAP.get(circuit);
        while (list != null && !list.isEmpty() && circuit.getTime() - list.getFirst().time > 60L) {
            list.removeFirst();
        }
        if (state.get(LIT)) {
            if (shouldUnpower) {
                circuit.setComponentState(pos, state.with(LIT, false), Component.NOTIFY_ALL);
                if (isBurnedOut(circuit, pos, true)) {
                    circuit.playSound(null, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f);
                    circuit.scheduleBlockTick(pos, circuit.getComponentState(pos).getComponent(), 160);
                }
            }
        } else if (!shouldUnpower && !isBurnedOut(circuit, pos, false)) {
            circuit.setComponentState(pos, state.with(LIT, true), Component.NOTIFY_ALL);
        }
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if (state.get(LIT) == this.shouldUnpower(circuit, pos, state) && !circuit.getCircuitTickScheduler().isTicking(pos, this)) {
            circuit.scheduleBlockTick(pos, this, 2);
        }
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.get(LIT) && state.get(FACING) != direction ? 15 : 0;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    protected boolean shouldUnpower(Circuit circuit, ComponentPos pos, ComponentState state) {
        FlatDirection direction = state.get(FACING).getOpposite();
        return circuit.isEmittingRedstonePower(pos.offset(direction), direction);
    }


    private static boolean isBurnedOut(ServerCircuit circuit, ComponentPos pos, boolean addNew) {
        List<BurnoutEntry> list = BURNOUT_MAP.computeIfAbsent(circuit, world -> Lists.newArrayList());
        if (addNew) {
            list.add(new BurnoutEntry(pos, circuit.getTime()));
        }
        int i = 0;
        for (BurnoutEntry burnoutEntry : list) {
            if (!burnoutEntry.pos.equals(pos) || ++i < 8) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    public static class BurnoutEntry {
        final ComponentPos pos;
        final long time;

        public BurnoutEntry(ComponentPos pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }
}
