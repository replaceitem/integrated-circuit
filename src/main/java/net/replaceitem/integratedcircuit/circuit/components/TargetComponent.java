package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jspecify.annotations.Nullable;

public class TargetComponent extends Component {
    public static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/target.png");
    public static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/target");

    public TargetComponent(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return TOOL_TEXTURE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, ITEM_TEXTURE, x, y, 0, a);
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
