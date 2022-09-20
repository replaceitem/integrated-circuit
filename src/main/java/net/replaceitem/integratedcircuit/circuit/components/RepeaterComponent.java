package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.RepeaterComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.Direction;

public class RepeaterComponent extends AbstractRedstoneGateComponent {
    public RepeaterComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.repeater"));
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/item/repeater.png");

    public static final Identifier TEXTURE_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/repeater_off.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/repeater_on.png");

    public static final Identifier TEXTURE_TORCH_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_top_off.png");
    public static final Identifier TEXTURE_TORCH_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_top_on.png");
    public static final Identifier TEXTURE_BAR = new IntegratedCircuitIdentifier("textures/integrated_circuit/repeater_bar.png");


    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof RepeaterComponentState repeaterComponentState)) throw new IllegalStateException("Invalid component state for component");
        final int size = IntegratedCircuitScreen.COMPONENT_SIZE;
        Direction renderedRotation = repeaterComponentState.getRotation().getOpposite();

        Identifier baseTexture = repeaterComponentState.isPowered() ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(matrices, baseTexture, x, y, renderedRotation.toInt(), 1, 1, 1, a);


        Identifier torchTexture = repeaterComponentState.isPowered() ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        
        IntegratedCircuitScreen.renderComponentPart(matrices, torchTexture, x, y, 6, 1, 4, 4, renderedRotation.toInt(),  1, 1, 1, a);

        boolean locked = repeaterComponentState.isLocked();
        Identifier knobTexture = locked ? TEXTURE_BAR : torchTexture;
        int knobOffsetAmount = repeaterComponentState.getDelay() * 2;
        if(locked) {
            IntegratedCircuitScreen.renderComponentPart(matrices, knobTexture, x, y, 2, 6 + knobOffsetAmount, 12, 2, renderedRotation.toInt(),  1, 1, 1, a);
        } else {
            IntegratedCircuitScreen.renderComponentPart(matrices, knobTexture, x, y, 6, 5 + knobOffsetAmount, 4, 4, renderedRotation.toInt(),  1, 1, 1, a);
        }
        

    }

    @Override
    public void onUse(ComponentState state, ServerCircuit circuit, ComponentPos pos) {
        if(!(state instanceof RepeaterComponentState repeaterComponentState)) throw new IllegalStateException("Invalid component state for component");
        circuit.setComponentState(pos, ((RepeaterComponentState) repeaterComponentState.copy()).cycleDelay(), Block.NOTIFY_ALL);
    }

    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        if(!(state instanceof RepeaterComponentState repeaterComponentState)) throw new IllegalStateException("Invalid component state for component");
        return (repeaterComponentState.getDelay() + 1) * 2;
    }

    @Override
    public ComponentState getPlacementState(ServerCircuit circuit, ComponentPos pos, Direction rotation) {
        RepeaterComponentState state = ((RepeaterComponentState) super.getPlacementState(circuit, pos, rotation));
        return state.setLocked(this.isLocked(circuit, pos, state));
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, Direction direction, ComponentState neighborState, ServerCircuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if(!(state instanceof RepeaterComponentState repeaterComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (direction.getAxis() != repeaterComponentState.getRotation().getAxis()) {
            return ((RepeaterComponentState) state.copy()).setLocked(this.isLocked(circuit, pos, state));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    @Override
    public boolean isLocked(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof RepeaterComponentState repeaterComponentState)) throw new IllegalStateException("Invalid component state for component");
        return this.getMaxInputLevelSides(circuit, pos, repeaterComponentState) > 0;
    }

    @Override
    protected boolean isValidInput(ComponentState state) {
        return isRedstoneGate(state);
    }

    @Override
    public ComponentState getDefaultState() {
        return new RepeaterComponentState(Direction.NORTH, false, 0, false);
    }

    @Override
    public ComponentState getState(byte data) {
        return new RepeaterComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
