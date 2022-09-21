package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ObserverComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.Direction;

public class ObserverComponent extends Component {
    public ObserverComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.observer"));
    }
    
    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/observer.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/observer_on.png");

    @Override
    public ComponentState getDefaultState() {
        return new ObserverComponentState(Direction.SOUTH, false);
    }

    @Override
    public ComponentState getState(byte data) {
        return new ObserverComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return TEXTURE;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        IntegratedCircuitScreen.renderComponentTexture(matrices, observerComponentState.isPowered() ? TEXTURE_ON : TEXTURE, x, y, observerComponentState.getRotation().getOpposite().toInt(), 1, 1, 1, a);
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (observerComponentState.isPowered()) {
            circuit.setComponentState(pos, ((ObserverComponentState) state.copy()).setPowered(false), Block.NOTIFY_LISTENERS);
        } else {
            circuit.setComponentState(pos, ((ObserverComponentState) state.copy()).setPowered(true), Block.NOTIFY_LISTENERS);
            circuit.createAndScheduleBlockTick(pos, this, 2);
        }
        this.updateNeighbors(circuit, pos, state);
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, Direction direction, ComponentState neighborState, ServerCircuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (observerComponentState.getRotation() == direction && !observerComponentState.isPowered()) {
            this.scheduleTick(circuit, pos);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    private void scheduleTick(ServerCircuit world, ComponentPos pos) {
        if (!world.getCircuitTickScheduler().isQueued(pos, this)) {
            world.createAndScheduleBlockTick(pos, this, 2);
        }
    }

    protected void updateNeighbors(ServerCircuit world, ComponentPos pos, ComponentState state) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        Direction direction = observerComponentState.getRotation();
        ComponentPos blockPos = pos.offset(direction.getOpposite());
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, ServerCircuit circuit, ComponentPos pos, Direction direction) {
        return state.getWeakRedstonePower(circuit, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, ServerCircuit circuit, ComponentPos pos, Direction direction) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (observerComponentState.isPowered() && observerComponentState.getRotation() == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    public void onBlockAdded(ComponentState state, ServerCircuit circuit, ComponentPos pos, ComponentState oldState) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (state.isOf(oldState.getComponent())) {
            return;
        }
        if (observerComponentState.isPowered() && !circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            ComponentState blockState = ((ObserverComponentState) observerComponentState.copy()).setPowered(false);
            circuit.setComponentState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            this.updateNeighbors(circuit, pos, blockState);
        }
    }

    @Override
    public void onStateReplaced(ComponentState state, ServerCircuit circuit, ComponentPos pos, ComponentState newState) {
        if(!(state instanceof ObserverComponentState observerComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (state.isOf(newState.getComponent())) {
            return;
        }
        if (observerComponentState.isPowered() && circuit.getCircuitTickScheduler().isQueued(pos, this)) {
            this.updateNeighbors(circuit, pos, ((ObserverComponentState) state.copy()).setPowered(false));
        }
    }

    @Override
    public ComponentState getPlacementState(ServerCircuit circuit, ComponentPos pos, Direction rotation) {
        return new ObserverComponentState(rotation, false);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, Direction direction) {
        return true;
    }
}
