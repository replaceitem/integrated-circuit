package net.replaceitem.integratedcircuit.client.circuit.renderer.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.circuit.renderer.ComponentRenderer;
import org.jspecify.annotations.Nullable;

import static net.replaceitem.integratedcircuit.circuit.components.LecternComponent.PAGE;

public class LecternComponentRenderer extends ComponentRenderer {
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/lectern.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float a, ComponentState state) {
        ComponentRenderer.extractComponentTextureRenderState(graphics, TEXTURE, x, y, 0, a);
        var textRenderer = Minecraft.getInstance().font;
        String text = String.valueOf(state.getValue(PAGE));

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 8, y + 8);
        graphics.pose().scale(.8f, .8f);
        graphics.pose().translate( (float) textRenderer.width(text) / -2, (float) textRenderer.lineHeight / -2 + 1);
        graphics.text(textRenderer, text, 0, 0, ARGB.color(a, CommonColors.BLACK), false);
        graphics.pose().popMatrix();
    }

    @Override
    public @Nullable Component getHoverInfoText(ComponentState state) {
        return Component.literal("Page " + state.getValue(PAGE));
    }
}
