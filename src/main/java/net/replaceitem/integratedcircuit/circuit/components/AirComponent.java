package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jetbrains.annotations.Nullable;

public class AirComponent extends Component {

    public AirComponent(int id, Settings settings) {
        super(id, settings);
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return null;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {}

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
