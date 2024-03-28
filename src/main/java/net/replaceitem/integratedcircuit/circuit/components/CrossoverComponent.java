package net.replaceitem.integratedcircuit.circuit.components;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.property.IntComponentProperty;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class CrossoverComponent extends AbstractConductingComponent {

    private static final IntComponentProperty POWER_X = new IntComponentProperty("power_x", 0, 4);
    private static final IntComponentProperty POWER_Y = new IntComponentProperty("power_y", 4, 4);


    public CrossoverComponent(int id, Settings settings) {
        super(id, settings);
    }

    protected static final Identifier TEXTURE_BRIDGE = new IntegratedCircuitIdentifier("textures/integrated_circuit/wire_bridge.png");
    protected static final Identifier ITEM_TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/crossover.png");

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return Text.literal("─ ")
                .append(IntegratedCircuitScreen.getSignalStrengthText(state.get(POWER_X)))
                .append("   │ ")
                .append(IntegratedCircuitScreen.getSignalStrengthText(state.get(POWER_Y)));
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        Vec3d colorX = RedstoneWireBlockAccessor.getCOLORS()[state.get(POWER_X)];
        Vec3d colorY = RedstoneWireBlockAccessor.getCOLORS()[state.get(POWER_Y)];

        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_X, x, y, 0, (float) colorX.x, (float) colorX.y, (float) colorX.z, a);
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_BRIDGE, x, y, 0, 1, 1, 1, a);
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE_Y, x, y, 0, (float) colorY.x, (float) colorY.y, (float) colorY.z, a);
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
        int powerX = getReceivedRedstonePower(circuit, pos, FlatDirection.Axis.X);
        int powerY = getReceivedRedstonePower(circuit, pos, FlatDirection.Axis.Y);
        if (state.get(POWER_X) != powerX || state.get(POWER_Y) != powerY) {
            if (circuit.getComponentState(pos) == state) {
                circuit.setComponentState(pos, state.with(POWER_X, powerX).with(POWER_Y, powerY), Component.NOTIFY_LISTENERS);
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
                j = Math.max(j, blockState.increasePower(direction.getOpposite()));
            }
        }
        return Math.max(i, j - 1);
    }


    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        if (!wiresGivePower) {
            return 0;
        }
        return direction.getAxis() == FlatDirection.Axis.X ? state.get(POWER_X) : state.get(POWER_Y);
    }

    @Override
    public int increasePower(ComponentState state, FlatDirection side) {
        return side.getAxis() == FlatDirection.Axis.X ? state.get(POWER_X) : state.get(POWER_Y);
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(POWER_X);
        builder.append(POWER_Y);
    }
}
