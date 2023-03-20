package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.CrossoverComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

import java.util.HashSet;

public class CrossoverComponent extends AbstractConductingComponent {
    public CrossoverComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.crossover"));
    }

    protected static final Identifier TEXTURE_BRIDGE = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_bridge.png");
    protected static final Identifier ITEM_TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/crossover.png");

    @Override
    public ComponentState getDefaultState() {
        return new CrossoverComponentState(0, 0);
    }

    @Override
    public ComponentState getState(byte data) {
        return new CrossoverComponentState(data);
    }

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        if(!(state instanceof CrossoverComponentState crossoverComponentState)) throw new IllegalStateException("Invalid component state for component");

        Vec3d colorX = RedstoneWireBlockAccessor.getCOLORS()[crossoverComponentState.getPowerX()];
        Vec3d colorY = RedstoneWireBlockAccessor.getCOLORS()[crossoverComponentState.getPowerY()];

        IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_X, x, y, 0, (float) colorX.x, (float) colorX.y, (float) colorX.z, a);
        IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_BRIDGE, x, y, 0, 1, 1, 1, a);
        IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE_Y, x, y, 0, (float) colorY.x, (float) colorY.y, (float) colorY.z, a);
    }











    @Override
    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        if (oldState.getComponent() == state.getComponent() || circuit.isClient) {
            return;
        }
        this.update(circuit, pos, state);
        this.updateOffsetNeighbors(circuit, pos);
    }

    @Override
    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        if(circuit.isClient) return;
        update(circuit, pos, state);
    }

    @Override
    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return this.getWeakRedstonePower(state, circuit, pos, direction);
    }


    @Override
    protected void update(Circuit circuit, ComponentPos pos, ComponentState state) {
        if(!(state instanceof CrossoverComponentState crossoverComponentState)) throw new IllegalStateException("Invalid component state for component");
        int powerX = getReceivedRedstonePower(circuit, pos, FlatDirection.Axis.X);
        int powerY = getReceivedRedstonePower(circuit, pos, FlatDirection.Axis.Y);
        if (crossoverComponentState.getPowerX() != powerX || crossoverComponentState.getPowerY() != powerY) {
            if (circuit.getComponentState(pos).equals(state)) {
                CrossoverComponentState newState = (CrossoverComponentState) state.copy();
                newState.setPowerX(powerX);
                newState.setPowerY(powerY);
                circuit.setComponentState(pos, newState, Component.NOTIFY_LISTENERS);
            }
            HashSet<ComponentPos> set = Sets.newHashSet();
            set.add(pos);
            for (FlatDirection direction : FlatDirection.VALUES) {
                set.add(pos.offset(direction));
            }
            for (ComponentPos blockPos : set) {
                circuit.updateNeighborsAlways(blockPos, this);
            }
        }
    }



    protected int getReceivedRedstonePower(Circuit circuit, ComponentPos pos, FlatDirection.Axis axis) {
        wiresGivePower = false;
        int i = 0;
        for (FlatDirection direction : FlatDirection.forAxis(axis)) {
            int power = circuit.getEmittedRedstonePower(pos.offset(direction), direction);
            if(power > i) i = power;
        }
        wiresGivePower = true;

        int j = 0;
        if (i < 15) {
            for (FlatDirection direction : FlatDirection.forAxis(axis)) {
                ComponentPos blockPos = pos.offset(direction);
                ComponentState blockState = circuit.getComponentState(blockPos);
                j = Math.max(j, increasePower(blockState, direction.getOpposite()));
            }
        }
        return Math.max(i, j - 1);
    }


    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if(!(state instanceof CrossoverComponentState crossoverComponentState)) throw new IllegalStateException("Invalid component state for component");
        if (!wiresGivePower) {
            return 0;
        }
        return direction.getAxis() == FlatDirection.Axis.X ? crossoverComponentState.getPowerX() : crossoverComponentState.getPowerY();
    }
}
