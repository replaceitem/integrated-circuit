package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class RedstoneBlockComponent extends Component {
    public RedstoneBlockComponent(int id, Settings settings) {
        super(id, settings);
    }

    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/redstone_block.png");

    @Override
    public Identifier getItemTexture() {
        return TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return IntegratedCircuitScreen.getSignalStrengthText(15);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.renderComponentTexture(drawContext, TEXTURE, x, y, 0, 1, 1, 1, a);
    }

    @Override
    public boolean emitsRedstonePower(ComponentState state) {
        return true;
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, FlatDirection direction) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return 15;
    }
}
