package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class TargetComponent extends Component {
    public TargetComponent(int id, Settings settings) {
        super(id, settings);
    }

    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/target.png");

    @Override
    public Identifier getItemTexture() {
        return TEXTURE;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.renderComponentTexture(matrices, TEXTURE, x, y, 0, 1, 1, 1, a);
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return true;
    }
}
