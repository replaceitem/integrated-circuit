package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jspecify.annotations.Nullable;

public class AirComponent extends Component {
    public AirComponent(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return null;
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }
}
