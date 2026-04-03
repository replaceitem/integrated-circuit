package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jspecify.annotations.Nullable;

public class LecternComponent extends Component {
    private static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/lectern.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/lectern");

    public static final IntegerProperty PAGE = IntegerProperty.create("page", 1, 15);
    
    public LecternComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(PAGE, 1));
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
        Font textRenderer = Minecraft.getInstance().font;
        String text = String.valueOf(state.getValue(PAGE));
        
        drawContext.pose().pushMatrix();
        drawContext.pose().translate(x + 8, y + 8);
        drawContext.pose().scale(.8f, .8f);
        drawContext.pose().translate( (float) textRenderer.width(text) / -2, (float) textRenderer.lineHeight / -2 + 1);
        drawContext.text(textRenderer, text, 0, 0, ARGB.color(a, CommonColors.BLACK), false);
        drawContext.pose().popMatrix();
    }

    @Override
    public net.minecraft.network.chat.Component getHoverInfoText(ComponentState state) {
        return net.minecraft.network.chat.Component.literal("Page " + state.getValue(PAGE));
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        if(circuit.isClient) {
            return;
        }
        state = state.cycle(PAGE);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        circuit.playSound(null, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, 1);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        super.onStateReplaced(state, circuit, pos, newState);
        if(!newState.isOf(this) || !newState.getValue(PAGE).equals(state.getValue(PAGE))) {
            circuit.updateComparators(pos, this);
        }
    }

    @Override
    public boolean hasComparatorOutput(ComponentState componentState) {
        return true;
    }

    @Override
    public int getComparatorOutput(ComponentState state, Circuit circuit, ComponentPos pos) {
        return state.getValue(PAGE);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(PAGE);
    }
}
