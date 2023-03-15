package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class PortComponent extends AbstractWireComponent {

    public PortComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.port"));
    }

    private static final Identifier TEXTURE_ARROW = new IntegratedCircuitIdentifier("textures/integrated_circuit/port.png");

    @Override
    public ComponentState getDefaultState() {
        return new PortComponentState(FlatDirection.NORTH, (byte) 0, false);
    }

    @Override
    public ComponentState getState(byte data) {
        return new PortComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return null;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        
        Vec3d color = RedstoneWireBlockAccessor.getCOLORS()[portComponentState.getPower()];
        float r = (float) color.x;
        float g = (float) color.y;
        float b = (float) color.z;

        FlatDirection rotation = portComponentState.getRotation();
        IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_ARROW, x, y, rotation.toInt(), r, g, b, a);
        
        Identifier wireTexture = rotation.getAxis() == FlatDirection.Axis.X ? TEXTURE_X : TEXTURE_Y;
        IntegratedCircuitScreen.renderComponentTexture(matrices, wireTexture, x, y, 0, r, g, b, a);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if(circuit.isClient) return;
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        PortComponentState newState = (PortComponentState) state.copy();
        newState.setRotation(portComponentState.getRotation().getOpposite());
        newState.setOutput(!portComponentState.isOutput());
        circuit.setComponentState(pos, newState, Component.NOTIFY_ALL);
    }

    @Override
    protected int getReceivedRedstonePower(Circuit world, ComponentPos pos) {
        ComponentState state = world.getComponentState(pos);
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(!portComponentState.isOutput()) return portComponentState.getPower();
        return super.getReceivedRedstonePower(world, pos);
    }

    public int getInternalPower(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(!portComponentState.isOutput()) return 0;
        return portComponentState.getPower();
    }

    public void assignExternalPower(ServerCircuit circuit, ComponentPos pos, ComponentState state, int newPower) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(portComponentState.isOutput()) return;
        if(portComponentState.getPower() == newPower) return;
        PortComponentState newState = ((PortComponentState) state.copy()).setPower(newPower);
        circuit.setComponentState(pos, newState, Component.NOTIFY_ALL);
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(!wiresGivePower) return 0;
        return portComponentState.getRotation().getOpposite() == direction ? portComponentState.getPower() : 0;
    }
}
