package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.state.*;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.IntComponentProperty;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class RepeaterComponent extends AbstractRedstoneGateComponent {

    private static final IntComponentProperty DELAY = new IntComponentProperty("delay", 3, 2);
    private static final BooleanComponentProperty LOCKED = new BooleanComponentProperty("locked", 5);

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
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(state.get(POWERED) ? 15 : 0);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        FlatDirection renderedRotation = state.get(FACING).getOpposite();

        Identifier baseTexture = state.get(POWERED) ? TEXTURE_ON : TEXTURE_OFF;
        IntegratedCircuitScreen.renderComponentTexture(matrices, baseTexture, x, y, renderedRotation.toInt(), 1, 1, 1, a);


        Identifier torchTexture = state.get(POWERED) ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        
        IntegratedCircuitScreen.renderPartialTexture(matrices, torchTexture, x, y, 6, 1, 4, 4, renderedRotation.toInt(),  1, 1, 1, a);

        boolean locked = state.get(LOCKED);
        Identifier knobTexture = locked ? TEXTURE_BAR : torchTexture;
        int knobOffsetAmount = state.get(DELAY) * 2;
        if(locked) {
            IntegratedCircuitScreen.renderPartialTexture(matrices, knobTexture, x, y, 2, 6 + knobOffsetAmount, 12, 2, renderedRotation.toInt(),  1, 1, 1, a);
        } else {
            IntegratedCircuitScreen.renderPartialTexture(matrices, knobTexture, x, y, 6, 5 + knobOffsetAmount, 4, 4, renderedRotation.toInt(),  1, 1, 1, a);
        }
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        circuit.setComponentState(pos, state.cycle(DELAY), Block.NOTIFY_ALL);
    }

    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return (state.get(DELAY) + 1) * 2;
    }

    @Override
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState state = super.getPlacementState(circuit, pos, rotation);
        return state.with(LOCKED, this.isLocked(circuit, pos, state));
    }

    @Override
    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        if (!circuit.isClient && direction.getAxis() != state.get(FACING).getAxis()) {
            return state.with(LOCKED, this.isLocked(circuit, pos, state));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, circuit, pos, neighborPos);
    }

    @Override
    public boolean isLocked(Circuit circuit, ComponentPos pos, ComponentState state) {
        return this.getMaxInputLevelSides(circuit, pos, state) > 0;
    }

    @Override
    protected boolean isValidInput(ComponentState state) {
        return isRedstoneGate(state);
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(DELAY, LOCKED);
    }
}
