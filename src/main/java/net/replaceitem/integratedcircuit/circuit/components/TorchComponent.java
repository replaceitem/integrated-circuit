package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Lists;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.*;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.TorchComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class TorchComponent extends Component {
    public TorchComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.torch"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/block/redstone_torch.png");

    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch.png");
    public static final Identifier TEXTURE_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_off.png");

    private static final Map<ServerCircuit, List<BurnoutEntry>> BURNOUT_MAP = new WeakHashMap<>();

    @Override
    public ComponentState getDefaultState() {
        return new TorchComponentState(FlatDirection.NORTH, true);
    }

    @Override
    public ComponentState getState(byte data) {
        return new TorchComponentState(data);
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        TorchComponentState componentState = (TorchComponentState) ((TorchComponentState) this.getDefaultState()).setRotation(rotation);
        if (!componentState.canPlaceAt(circuit, pos)) {
            for (FlatDirection value : CircuitNeighborUpdater.UPDATE_ORDER) {
                componentState.setRotation(value);
                if(componentState.canPlaceAt(circuit, pos)) return componentState;
            }
            return null;
        }
        return componentState;
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        return IntegratedCircuitScreen.getSignalStrengthText(torchComponentState.isLit() ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        Identifier texture = torchComponentState.isLit()?TEXTURE:TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(matrices, texture, x, y, torchComponentState.getRotation().toInt(), 1, 1, 1, a);
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (direction.getOpposite() == torchComponentState.getRotation() && !canPlaceAt(torchComponentState, circuit, pos)) {
            return Components.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public boolean canPlaceAt(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        FlatDirection direction = torchComponentState.getRotation();
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
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        boolean shouldUnpower = this.shouldUnpower(circuit, pos, state);
        List<BurnoutEntry> list = BURNOUT_MAP.get(circuit);
        while (list != null && !list.isEmpty() && circuit.getTime() - list.get(0).time > 60L) {
            list.remove(0);
        }
        if (torchComponentState.isLit()) {
            if (shouldUnpower) {
                circuit.setComponentState(pos, ((TorchComponentState) torchComponentState.copy()).setLit(false), Component.NOTIFY_ALL);
                if (isBurnedOut(circuit, pos, true)) {
                    circuit.scheduleBlockTick(pos, circuit.getComponentState(pos).getComponent(), 160);
                }
            }
        } else if (!shouldUnpower && !isBurnedOut(circuit, pos, false)) {
            circuit.setComponentState(pos, ((TorchComponentState) torchComponentState.copy()).setLit(true), Component.NOTIFY_ALL);
        }
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (torchComponentState.isLit() == this.shouldUnpower(circuit, pos, state) && !circuit.getCircuitTickScheduler().isTicking(pos, this)) {
            circuit.scheduleBlockTick(pos, this, 2);
        }
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        return torchComponentState.isLit() ? 15 : 0;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
    
    protected boolean shouldUnpower(Circuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof TorchComponentState torchComponentState)) throw new IllegalStateException("Invalid component state for component");
        FlatDirection direction = torchComponentState.getRotation().getOpposite();
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
}
