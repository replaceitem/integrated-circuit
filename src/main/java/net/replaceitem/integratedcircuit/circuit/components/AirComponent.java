package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.Components;

public class AirComponent extends Component {

    public AirComponent(int id) {
        super(id, Text.translatable("component.integrated_circuit.air"));
    }

    @Override
    public ComponentState getDefaultState() {
        return Components.AIR_DEFAULT_STATE;
    }

    @Override
    public Identifier getItemTexture() {
        return null;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {}

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
