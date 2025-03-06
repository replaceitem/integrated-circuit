package net.replaceitem.integratedcircuit.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;

import java.time.Duration;

public class ToolboxButton extends ClickableWidget {

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
        super(x, y, SIZE, SIZE, Text.empty());
        this.setTooltip(Tooltip.of(component.getName()));
        this.setTooltipDelay(Duration.ofMillis(700));
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(
            RenderLayer::getGuiTextured,
            selectBackgroundTexture(),
            getX(),
            getY(),
            SIZE,
            SIZE
        );

        Identifier toolTexture = component.getToolTexture();
        if (toolTexture != null) renderPaletteItem(context, toolTexture);
    }

    private void renderPaletteItem(DrawContext drawContext, Identifier itemTexture) {
        drawContext.drawTexture(
            RenderLayer::getGuiTextured,
            itemTexture,
            getX(),
            getY(),
            0,
            0,
            SIZE,
            SIZE,
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return Text.translatable("gui.narrate.button", component.getName());
    }
}
