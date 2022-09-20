package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TickPriority;
import net.replaceitem.integratedcircuit.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.AbstractRedstoneGateComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ComparatorComponentState;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;

public class ComparatorComponent extends AbstractRedstoneGateComponent {
    public ComparatorComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.comparator"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/item/comparator.png");

    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/comparator.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/comparator_on.png");

    public static final Identifier TEXTURE_TORCH_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_top_off.png");
    public static final Identifier TEXTURE_TORCH_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_top_on.png");
    
    @Override
    public ComponentState getDefaultState() {
        return getState((byte) 0);
    }

    @Override
    public ComponentState getState(byte data) {
        return new ComparatorComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof ComparatorComponentState comparatorComponentState)) throw new IllegalStateException("Invalid component state for component");
        
        boolean powered = comparatorComponentState.isPowered();
        IntegratedCircuitScreen.renderComponentTexture(matrices, powered ? TEXTURE_ON : TEXTURE, x, y, comparatorComponentState.getRotation().getOpposite().toInt(), 1, 1, 1, a);
        
        Identifier torchTexture = powered ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;

        IntegratedCircuitScreen.renderComponentPart(matrices, torchTexture, x, y, 3, 10, 4, 4, comparatorComponentState.getRotation().getOpposite().toInt(), 1, 1, 1, a);
        IntegratedCircuitScreen.renderComponentPart(matrices, torchTexture, x, y, 9, 10, 4, 4, comparatorComponentState.getRotation().getOpposite().toInt(), 1, 1, 1, a);

        Identifier modeTorchTexture = comparatorComponentState.isSubtractMode() ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        IntegratedCircuitScreen.renderComponentPart(matrices, modeTorchTexture, x, y, 6, 1, 4, 4, comparatorComponentState.getRotation().getOpposite().toInt(), 1, 1, 1, a);
    }


    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return 2;
    }

    @Override
    protected int getOutputLevel(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof ComparatorComponentState comparatorComponentState)) throw new IllegalStateException("Invalid component state for component");
        return comparatorComponentState.getOutputSignal();
    }

    private int calculateOutputSignal(ServerCircuit world, ComponentPos pos, ComparatorComponentState state) {
        int i = this.getPower(world, pos, state);
        if (i == 0) {
            return 0;
        }
        int j = this.getMaxInputLevelSides(world, pos, state);
        if (j > i) {
            return 0;
        }
        if (state.isSubtractMode()) {
            return i - j;
        }
        return i;
    }

    @Override
    protected boolean hasPower(ServerCircuit circuit, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        if(!(state instanceof ComparatorComponentState comparatorComponentState)) throw new IllegalStateException("Invalid component state for component");
        int i = this.getPower(circuit, pos, state);
        if (i == 0) {
            return false;
        }
        int j = this.getMaxInputLevelSides(circuit, pos, state);
        if (i > j) {
            return true;
        }
        return i == j && !comparatorComponentState.isSubtractMode();
    }


    @Override
    protected int getPower(ServerCircuit circuit, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        // overriding is unnecessary, but when blocks get added that have a comparator power level, this needs to be changed
        return super.getPower(circuit, pos, state);
    }

    @Override
    public void onUse(ComponentState state, ServerCircuit circuit, ComponentPos pos) {
        if(!(state instanceof ComparatorComponentState comparatorComponentState)) throw new IllegalStateException("Invalid component state for component");
        comparatorComponentState = ((ComparatorComponentState) state.copy()).setSubtractMode(!comparatorComponentState.isSubtractMode());
        circuit.setComponentState(pos, comparatorComponentState, Block.NOTIFY_LISTENERS);
        this.update(circuit, pos, comparatorComponentState);
    }

    @Override
    protected void updatePowered(ServerCircuit world, ComponentPos pos, AbstractRedstoneGateComponentState state) {
        if(!(state instanceof ComparatorComponentState comparatorComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (world.getCircuitTickScheduler().isTicking(pos, this)) {
            return;
        }
        int calculatedOutputSignal = this.calculateOutputSignal(world, pos, comparatorComponentState);
        int outputSignal = comparatorComponentState.getOutputSignal();
        if (calculatedOutputSignal != outputSignal || state.isPowered() != this.hasPower(world, pos, state)) {
            TickPriority tickPriority = this.isTargetNotAligned(world, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
            world.createAndScheduleBlockTick(pos, this, 2, tickPriority);
        }
    }

    private void update(ServerCircuit world, ComponentPos pos, ComparatorComponentState state) {
        int i = this.calculateOutputSignal(world, pos, state);
        int j = 0;
        j = state.getOutputSignal();
        state.setOutputSignal(i);
        if (j != i || !state.isSubtractMode()) {
            boolean hasPower = this.hasPower(world, pos, state);
            boolean powered = state.isPowered();
            if (powered && !hasPower) {
                world.setComponentState(pos, ((ComparatorComponentState) state.copy()).setPowered(false), Block.NOTIFY_LISTENERS);
            } else if (!powered && hasPower) {
                world.setComponentState(pos, ((ComparatorComponentState) state.copy()).setPowered(true), Block.NOTIFY_LISTENERS);
            }
            this.updateTarget(world, pos, state);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        if(!(state instanceof ComparatorComponentState comparatorComponentState)) throw new IllegalStateException("Invalid component state for component");
        this.update(circuit, pos, comparatorComponentState);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
