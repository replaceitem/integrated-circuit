package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ServerCircuitContext;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.FlatDirectionComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.IntComponentProperty;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import org.jetbrains.annotations.Nullable;

public class PortComponent extends AbstractWireComponent {

    public static final FlatDirectionComponentProperty FACING = new FlatDirectionComponentProperty("facing", 0);
    public static final IntComponentProperty POWER = new IntComponentProperty("power", 3, 4);
    public static final BooleanComponentProperty IS_OUTPUT = new BooleanComponentProperty("is_output", 7);

    public PortComponent(int id, Settings settings) {
        super(id, settings);
    }

    private static final Identifier TEXTURE_ARROW = new IntegratedCircuitIdentifier("textures/integrated_circuit/port.png");

    @Override
    public @Nullable Identifier getItemTexture() {
        return null;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        Vec3d color = RedstoneWireBlockAccessor.getCOLORS()[state.get(POWER)];
        float r = (float) color.x;
        float g = (float) color.y;
        float b = (float) color.z;

        FlatDirection rotation = state.get(FACING);
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_ARROW, x, y, rotation.getIndex(), r, g, b, a);
        
        Identifier wireTexture = rotation.getAxis() == FlatDirection.Axis.X ? TEXTURE_X : TEXTURE_Y;
        IntegratedCircuitScreen.renderComponentTexture(drawContext, wireTexture, x, y, 0, r, g, b, a);
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
    protected int getReceivedRedstonePower(Circuit world, ComponentPos pos) {
        ComponentState state = world.getComponentState(pos);
        if(!state.get(IS_OUTPUT)) return state.get(POWER);
        return super.getReceivedRedstonePower(world, pos);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!wiresGivePower) return 0;
        return state.get(FACING).getOpposite() == direction ? state.get(POWER) : 0;
    }

    @Override
    protected ComponentProperty<Integer> getPowerProperty() {
        return PortComponent.POWER;
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return state.get(POWER);
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(FACING);
        builder.append(POWER);
        builder.append(IS_OUTPUT);
    }
}
