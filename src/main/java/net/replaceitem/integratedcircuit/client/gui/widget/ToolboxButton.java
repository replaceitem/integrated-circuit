package net.replaceitem.integratedcircuit.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;

import java.time.Duration;

public class ToolboxButton extends AbstractWidget {

    public static final Identifier COMPONENT_BUTTON_TEXTURE_IDLE = IntegratedCircuit.id(
        "toolbox/button_bg"
    );

    public static final Identifier COMPONENT_BUTTON_TEXTURE_HOVER = IntegratedCircuit.id(
        "toolbox/button_bg_hover"
    );

    public static final Identifier COMPONENT_BUTTON_TEXTURE_SELECTED = IntegratedCircuit.id(
        "toolbox/button_bg_selected"
    );

    public static final int SIZE = 16;
    public static final int MARGIN = 2;

    protected final Component component;
    protected boolean selected;

    public ToolboxButton(int x, int y, Component component) {
        super(x, y, SIZE, SIZE, net.minecraft.network.chat.Component.empty());
        this.setTooltip(Tooltip.create(component.getName()));
        this.setTooltipDelay(Duration.ofMillis(700));
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    @Override
    public boolean isHoveredOrFocused() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                selectBackgroundTexture(),
                getX(),
                getY(),
                SIZE,
                SIZE
        );

        Identifier toolTexture = component.getToolTexture();

        if (toolTexture != null)
            renderPaletteItem(graphics, toolTexture);
    }

    private void renderPaletteItem(GuiGraphicsExtractor graphics, Identifier itemTexture) {
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            itemTexture,
            getX(),
            getY(),
            SIZE,
            SIZE
        );
    }

    private Identifier selectBackgroundTexture() {
        if (selected) {
            return COMPONENT_BUTTON_TEXTURE_SELECTED;
        }

        if (isHovered()) {
            return COMPONENT_BUTTON_TEXTURE_HOVER;
        }

        return COMPONENT_BUTTON_TEXTURE_IDLE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return net.minecraft.network.chat.Component.translatable("gui.narrate.button", component.getName());
    }
}
