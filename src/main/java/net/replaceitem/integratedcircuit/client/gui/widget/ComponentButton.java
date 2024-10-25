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

public class ComponentButton extends ClickableWidget {

    public static final Identifier COMPONENT_BUTTON_TEXTURE = IntegratedCircuit.id("container/integrated_circuit/component_button");
    public static final Identifier COMPONENT_BUTTON_TEXTURE_HIGHLIGHTED = IntegratedCircuit.id("container/integrated_circuit/component_button_highlighted");

    public static final int SIZE = 14;
    
    protected final Component component;
    protected boolean selected;

    public ComponentButton(int x, int y, Component component) {
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
        context.drawGuiTexture(RenderLayer::getGuiTextured, selected ? COMPONENT_BUTTON_TEXTURE_HIGHLIGHTED : COMPONENT_BUTTON_TEXTURE, getX(), getY(), SIZE, SIZE);
        Identifier itemTexture = component.getItemTexture();
        if(itemTexture != null) renderPaletteItem(context, itemTexture);
    }
    
    private void renderPaletteItem(DrawContext drawContext, Identifier itemTexture) {
        drawContext.drawTexture(RenderLayer::getGuiTextured, itemTexture, getX()+1, getY()+1, 0, 0, SIZE-2, SIZE-2, SIZE-2, SIZE-2);
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
