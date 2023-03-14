package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.state.*;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;

import java.util.HashSet;

public class PortComponent extends Component {

    public PortComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.port"));
    }

    private static final Identifier TEXTURE_ARROW = new IntegratedCircuitIdentifier("textures/integrated_circuit/port.png");
    private static final Identifier TEXTURE_X = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_x.png");
    private static final Identifier TEXTURE_Y = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_y.png");


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
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(circuit.isClient) return;
        if(portComponentState.isOutput()) {
            int power = getReceivedRedstonePower(circuit, pos);
            if (power != portComponentState.getPower()) {
                circuit.setComponentState(pos, ((PortComponentState) state.copy()).setPower(power), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        if (state.isOf(newState.getComponent())) {
            return;
        }
        super.onStateReplaced(state, circuit, pos, newState);
        if(circuit.isClient) return;
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        PortComponentState newState = (PortComponentState) state.copy();
        newState.setRotation(portComponentState.getRotation().getOpposite());
        newState.setOutput(!portComponentState.isOutput());
        circuit.setComponentState(pos, newState, Block.NOTIFY_LISTENERS);
    }

    private void updateOffsetNeighbors(Circuit circuit, ComponentPos pos) {
        for (FlatDirection direction : FlatDirection.VALUES) {
            this.updateNeighbors(circuit, pos.offset(direction));
        }
    }

    private void updateNeighbors(Circuit circuit, ComponentPos pos) {
        ComponentState componentState = circuit.getComponentState(pos);
        if (!(componentState.isOf(this) || componentState.isOf(Components.WIRE))) {
            return;
        }
        circuit.updateNeighborsAlways(pos, this);
        for (FlatDirection direction : FlatDirection.VALUES) {
            circuit.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    private int getReceivedRedstonePower(Circuit world, ComponentPos pos) {
        Components.WIRE.wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        Components.WIRE.wiresGivePower = true;
        int j = 0;
        if (i < 15) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                ComponentPos blockPos = pos.offset(direction);
                ComponentState blockState = world.getComponentState(blockPos);
                j = Math.max(j, increasePower(blockState));
            }
        }
        return Math.max(i, j - 1);
    }
    
    public int getInternalPower(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(!portComponentState.isOutput()) return 0;
        return portComponentState.getPower();
    }

    public void assignExternalPower(ServerCircuit circuit, ComponentPos pos, ComponentState state, int newPower) {
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        
        if(portComponentState.isOutput()) return;
        if(portComponentState.getPower() != newPower) {
            if (circuit.getComponentState(pos).equals(state)) {
                PortComponentState newState = (PortComponentState) state.copy();
                newState.setPower(newPower);
                circuit.setComponentState(pos, newState, Component.NOTIFY_LISTENERS);
            }
            HashSet<ComponentPos> set = Sets.newHashSet();
            set.add(pos);
            for (FlatDirection updateDirection : FlatDirection.VALUES) {
                set.add(pos.offset(updateDirection));
            }
            for (ComponentPos blockPos : set) {
                circuit.updateNeighborsAlways(blockPos, this);
            }
        }
    }
    
    private int increasePower(ComponentState blockState) {
        if(blockState instanceof PortComponentState portComponentState) return portComponentState.getPower();
        return blockState instanceof WireComponentState wireComponentState ? wireComponentState.getPower() : 0;
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
        if(!(state instanceof PortComponentState portComponentState)) throw new IllegalStateException("Invalid component state for component");
        if(!Components.WIRE.wiresGivePower) return 0;
        return portComponentState.getRotation().getOpposite() == direction ? portComponentState.getPower() : 0;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
