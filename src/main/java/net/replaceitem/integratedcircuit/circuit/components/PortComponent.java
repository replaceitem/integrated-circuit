package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class PortComponent extends AbstractWireComponent {

    public static final EnumProperty<FlatDirection> FACING = FacingComponent.FACING;
    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty IS_OUTPUT = BooleanProperty.of("is_output");

    public PortComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, FlatDirection.NORTH).with(POWER, 0).with(IS_OUTPUT, false));
    }

    private static final Identifier TEXTURE_ARROW = IntegratedCircuit.id("textures/integrated_circuit/port.png");

    @Override
    public @Nullable Identifier getItemTexture() {
        return null;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        int color = RedstoneWireBlock.getWireColor(state.get(POWER));
        
        FlatDirection rotation = state.get(FACING);
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_ARROW, x, y, rotation.getIndex(), color);
        
        Identifier wireTexture = rotation.getAxis() == FlatDirection.Axis.X ? TEXTURE_X : TEXTURE_Y;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, wireTexture, x, y, 0, color);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if(circuit.isClient) return;
        ServerCircuitContext context = ((ServerCircuit) circuit).getContext();
        FlatDirection portSide = Circuit.getPortSide(pos);
        boolean isOutput = state.get(IS_OUTPUT);
        boolean wasOutput = oldState.get(IS_OUTPUT);
        context.setRenderStrength(portSide, state.get(POWER));
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
        if(isOutput || wasOutput) context.updateExternal(portSide);
        if(!isOutput && wasOutput) context.readExternalPower(portSide);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        state = state.with(FACING, state.get(FACING).getOpposite()).cycle(IS_OUTPUT);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
    }

    @Override
    protected int getReceivedRedstonePower(Circuit circuit, ComponentPos pos) {
        ComponentState state = circuit.getComponentState(pos);
        if(!state.get(IS_OUTPUT)) return state.get(POWER);
        return super.getReceivedRedstonePower(circuit, pos);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!wiresGivePower) return 0;
        return state.get(FACING).getOpposite() == direction ? state.get(POWER) : 0;
    }

    @Override
    protected IntProperty getPowerProperty() {
        return PortComponent.POWER;
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return state.get(POWER);
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
        builder.add(POWER);
        builder.add(IS_OUTPUT);
    }
}
