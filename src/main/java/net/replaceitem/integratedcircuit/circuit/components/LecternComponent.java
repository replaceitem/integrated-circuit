package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import org.jetbrains.annotations.Nullable;

public class LecternComponent extends Component {
    private static final Identifier ITEM_TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/lectern.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("textures/gui/newui/toolbox/icons/lectern.png");

    public static final IntProperty PAGE = IntProperty.of("page", 1, 15);
    
    public LecternComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(PAGE, 1));
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
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        IntegratedCircuitScreen.renderComponentTexture(drawContext, ITEM_TEXTURE, x, y, 0, a);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String text = String.valueOf(state.get(PAGE));
        
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(x + 8, y + 8, 0);
        drawContext.getMatrices().scale(.8f, .8f, 1);
        drawContext.getMatrices().translate( (float) textRenderer.getWidth(text) / -2, (float) textRenderer.fontHeight / -2 + 1, 0);
        drawContext.drawText(textRenderer, text, 0, 0, 0, false);
        drawContext.getMatrices().pop();
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        return Text.literal("Page " + state.get(PAGE));
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        if(circuit.isClient) {
            return;
        }
        state = state.cycle(PAGE);
        circuit.setComponentState(pos, state, Component.NOTIFY_ALL);
        circuit.updateNeighborsAlways(pos, this);
        circuit.playSound(null, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        super.onStateReplaced(state, circuit, pos, newState);
        if(!newState.isOf(this) || !newState.get(PAGE).equals(state.get(PAGE))) {
            circuit.updateComparators(pos, this);
        }
    }

    @Override
    public boolean hasComparatorOutput(ComponentState componentState) {
        return true;
    }

    @Override
    public int getComparatorOutput(ComponentState state, Circuit circuit, ComponentPos pos) {
        return state.get(PAGE);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(PAGE);
    }
}
