package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class ObserverComponent extends FacingComponent {

    private static final BooleanComponentProperty POWERED = new BooleanComponentProperty("powered", 3);

    public ObserverComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.observer"));
    }
    
    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/observer.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/observer_on.png");

    @Override
    public Identifier getItemTexture() {
        return TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(POWERED) ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.renderComponentTexture(matrices, state.get(POWERED) ? TEXTURE_ON : TEXTURE, x, y, state.get(FACING).getOpposite().toInt(), 1, 1, 1, a);
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if (state.get(POWERED)) {
            circuit.setComponentState(pos, state.with(POWERED, false), Block.NOTIFY_LISTENERS);
        } else {
            circuit.setComponentState(pos, state.with(POWERED, true), Block.NOTIFY_LISTENERS);
            circuit.scheduleBlockTick(pos, this, 2);
        }
        this.updateNeighbors(circuit, pos, state);
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (state.get(FACING) == direction && !state.get(POWERED)) {
            this.scheduleTick(circuit, pos);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    private void scheduleTick(Circuit circuit, ComponentPos pos) {
        if (!circuit.isClient && !circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            circuit.scheduleBlockTick(pos, this, 2);
        }
    }

    protected void updateNeighbors(Circuit world, ComponentPos pos, ComponentState state) {
        FlatDirection direction = state.get(FACING);
        ComponentPos blockPos = pos.offset(direction.getOpposite());
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return state.getWeakRedstonePower(circuit, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (state.get(POWERED) && state.get(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (state.isOf(oldState.getComponent())) {
            return;
        }
        if(!circuit.isClient && state.get(POWERED) && !circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            ComponentState blockState = state.with(POWERED, false);
            circuit.setComponentState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            this.updateNeighbors(circuit, pos, blockState);
        }
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if (state.isOf(newState.getComponent())) {
            return;
        }
        if (!circuit.isClient && state.get(POWERED) && circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            this.updateNeighbors(circuit, pos, state.with(POWERED, false));
        }
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, FlatDirection direction) {
        return true;
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(POWERED);
    }
}
