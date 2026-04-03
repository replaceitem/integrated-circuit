package net.replaceitem.integratedcircuit.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.*;
import net.replaceitem.integratedcircuit.circuit.components.FacingComponent;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;
import net.replaceitem.integratedcircuit.client.gui.widget.ToolSelectionInfo;
import net.replaceitem.integratedcircuit.client.gui.widget.Toolbox;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class IntegratedCircuitScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = IntegratedCircuit.id(
        "textures/gui/container/integrated_circuit.png"
    );

    protected static final int BACKGROUND_WIDTH = 302;
    protected static final int BACKGROUND_HEIGHT = 250;

    public static final int COMPONENT_SIZE = 16;
    public static final int RENDER_COMPONENT_SIZE = 12;

    private static final float RENDER_SCALE = (((float) RENDER_COMPONENT_SIZE) / ((float) COMPONENT_SIZE));

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 9;

    private static final int GRID_X = 101;
    private static final int GRID_Y = 36;

    private static final int CIRCUIT_NAME_TEXTBOX_X = 153;
    private static final int CIRCUIT_NAME_TEXTBOX_Y = 9;
    private static final int CIRCUIT_NAME_TEXTBOX_WIDTH = 132;
    private static final int CIRCUIT_NAME_TEXTBOX_HEIGHT = 12;

    private static final int TOOLBOX_X = 8;
    private static final int TOOLBOX_Y = 24;

    private static final int STATUSBAR_X = 89;
    private static final int STATUSBAR_Y = 234;
    private static final int STATUSBAR_RIGHT_MARGIN = 8;

    private boolean startedDraggingInside = false;

    protected int x, y;
    protected int titleX, titleY;

    @Nullable
    protected Toolbox toolbox;
    @Nullable
    protected EditBox customNameTextField;

    protected Component customName;
    protected final ClientCircuit circuit;

    private FlatDirection cursorRotation = FlatDirection.NORTH;
    private @Nullable ComponentState cursorState = null;

    public IntegratedCircuitScreen(ClientCircuit circuit, Component customName) {
        super(Component.translatable("integrated_circuit.gui.screen_title"));

        this.circuit = circuit;
        this.customName = customName;
    }

    @Override
    protected void init() {
        this.x = (this.width - BACKGROUND_WIDTH) / 2;
        this.y = (this.height - BACKGROUND_HEIGHT) / 2;
        this.titleX = this.x + TITLE_X;
        this.titleY = this.y + TITLE_Y;

        this.toolbox = new Toolbox(this, TOOLBOX_X, TOOLBOX_Y);
        this.toolbox.init();
        this.toolbox.registerToolSelectionSubscriber(this::updateToolSelection);

        this.customNameTextField = new EditBox(
            this.font,
            this.x + CIRCUIT_NAME_TEXTBOX_X,
            this.y + CIRCUIT_NAME_TEXTBOX_Y,
            CIRCUIT_NAME_TEXTBOX_WIDTH,
            CIRCUIT_NAME_TEXTBOX_HEIGHT,
            this.customName
        ) {
            @Override
            public void setFocused(boolean focused) {
                super.setFocused(focused);

                if (!focused) {
                    circuit.rename(customName);
                }
            }
        };

        this.customNameTextField.setBordered(false);
        this.customNameTextField.setMaxLength(50);
        this.customNameTextField.setEditable(true);
        this.customNameTextField.setValue(this.customName.getString());

        this.customNameTextField.setHint(
            Component.translatable("integrated_circuit.gui.rename_field_placeholder")
                .withStyle(style ->
                        style.withColor(0x9C9C9C)
                             .withShadowColor(0x22222222))
        );

        this.customNameTextField.setResponder(
            name -> this.customName = Component.literal(name)
        );

        this.addWidget(this.customNameTextField);
        this.addRenderableWidget(this.customNameTextField);
    }

    public void updateToolSelection(ToolSelectionInfo selectionInfo) {
        updateCursorState(selectionInfo.component());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        ClientPlayNetworking.send(new FinishEditingC2SPacket(this.circuit.getContext().getBlockPos()));
        super.onClose();
    }

    public ClientCircuit getClientCircuit() {
        return circuit;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.text(this.font, this.title, this.titleX, this.titleY, CommonColors.DARK_GRAY, false);
        this.renderStatusBar(graphics, mouseX, mouseY);
        this.renderContent(graphics);
        this.renderCursorState(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                x, y,
                0, 0,
                BACKGROUND_WIDTH,
                BACKGROUND_HEIGHT,
                512,
                512
        );
    }



    private void renderStatusBar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        ComponentState componentState = circuit.getComponentState(pos);
        net.replaceitem.integratedcircuit.circuit.Component component = componentState.getComponent();

        int gridX = getGridXAt(mouseX);
        int gridY = getGridYAt(mouseY);

        Component leftSideText = null;
        Component rightSideText = null;

        if (circuit.isInside(pos)) {
            String componentName = component != Components.AIR
                ? component.getName().getString()
                : "";

            leftSideText = Component.literal(
                String.format(
                    "(%d, %d) %s",
                    gridX,
                    gridY,
                    componentName
                )
            );
        } else if (componentState.getComponent() instanceof PortComponent portComponent) {
            leftSideText = portComponent.getName();
        }

        if (component != Components.AIR) {
            rightSideText = componentState.getHoverInfoText();
        }

        if (leftSideText != null) {
            graphics.text(
                this.font,
                leftSideText,
                this.x + STATUSBAR_X,
                this.y + STATUSBAR_Y,
                CommonColors.DARK_GRAY,
                false
            );
        }

        if (rightSideText != null) {
            int componentInfoWidth = this.font.width(rightSideText);

            graphics.text(
                this.font,
                rightSideText,
                this.x + BACKGROUND_WIDTH - componentInfoWidth - STATUSBAR_RIGHT_MARGIN,
                this.y + STATUSBAR_Y,
                CommonColors.DARK_GRAY,
                false
            );
        }
    }

    public static Component getSignalStrengthText(int signalStrength) {
        int color = RedStoneWireBlock.getColorForPower(signalStrength);
        return Component.literal(String.valueOf(signalStrength)).withStyle(style -> style.withColor(color));
    }

    private void renderCursorState(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        boolean validSpot = circuit.getComponentState(pos).isAir();
        float a = validSpot ? 0.5f : 0.2f;
        if (this.cursorState != null && circuit.isInside(pos)) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(getGridPosX(0), getGridPosY(0));
            graphics.pose().scale(RENDER_SCALE, RENDER_SCALE);
            extractComponentStateRenderStateInGrid(graphics, this.cursorState, pos.getX(), pos.getY(), a);
            graphics.pose().popMatrix();
        }
    }

    protected void renderContent(GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(getGridPosX(0), getGridPosY(0));

        graphics.pose().scale(RENDER_SCALE, RENDER_SCALE);

        for (FlatDirection direction : FlatDirection.VALUES) {
            ComponentState port = circuit.getPorts()[direction.getIndex()];
            ComponentPos pos = Circuit.PORT_POSITIONS.get(direction);
            extractComponentStateRenderStateInGrid(graphics, port, pos.getX(), pos.getY(), 1);
        }

        for (int i = 0; i < Circuit.SIZE; i++) {
            for (int j = 0; j < Circuit.SIZE; j++) {
                ComponentState componentState = circuit.getSection().getComponentState(i, j);
                extractComponentStateRenderStateInGrid(graphics, componentState, i, j, 1);
            }
        }

        graphics.pose().popMatrix();
    }

    protected static void extractComponentStateRenderState(GuiGraphicsExtractor graphics, ComponentState state, int x, int y, float a) {
        state.getComponent().extractRenderState(graphics, x, y, a, state);
    }

    protected void extractComponentStateRenderStateInGrid(GuiGraphicsExtractor graphics, ComponentState state, int x, int y, float a) {
        extractComponentStateRenderState(graphics, state, x * COMPONENT_SIZE, y * COMPONENT_SIZE, a);
    }

    public static void extractComponentTextureRenderState(GuiGraphicsExtractor graphics, Identifier component, int x, int y, int rot, float alpha) {
        extractComponentTextureRenderState(graphics, component, x, y, rot, ARGB.white(alpha));
    }

    public static void extractComponentTextureRenderState(GuiGraphicsExtractor graphics, Identifier component, int x, int y, int rot, int color) {
        extractComponentTextureRenderState(graphics, component, x, y, rot, color, 0, 0, 16, 16);
    }

    public static void extractComponentTextureRenderState(GuiGraphicsExtractor graphics, Identifier component, int x, int y, int rot, int color, int u, int v, int w, int h) {
        extractPartialTextureRenderState(graphics, component, x, y, u, v, 16, 16, rot, color, u, v, w, h);
    }


    public static void extractPartialTextureRenderState(GuiGraphicsExtractor graphics, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float alpha) {
        extractPartialTextureRenderState(graphics, texture, componentX, componentY, x, y, textureW, textureH, rot, ARGB.white(alpha));
    }

    public static void extractPartialTextureRenderState(GuiGraphicsExtractor graphics, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, int color) {
        extractPartialTextureRenderState(graphics, texture, componentX, componentY, x, y, textureW, textureH, rot, color, 0, 0, textureW, textureH);
    }

    private static void extractPartialTextureRenderState(GuiGraphicsExtractor graphics, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, int color, int u, int v, int w, int h) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(componentX + 8, componentY + 8);
        graphics.pose().rotate((float) (rot * Math.PI * 0.5));
        graphics.pose().translate(-8, -8);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, textureW, textureH, color);
        graphics.pose().popMatrix();
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T drawableElement) {
        return super.addRenderableWidget(drawableElement);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if(this.minecraft.player == null) return true;

        ComponentPos clickedPos = getComponentPosAt((int) click.x(), (int) click.y());

        if (customNameTextField != null && customNameTextField.isFocused() && !customNameTextField.isMouseOver(click.x(), click.y())) {
            customNameTextField.setFocused(false);
        }

        if (matchesMouse(DefaultConfig.getConfig().getRotateKeybind(), click.button())) {
            rotateComponent(1);
            return true;
        }

        boolean isInCircuit = circuit.isInside(clickedPos);
        boolean isPlace = matchesMouse(DefaultConfig.getConfig().getPlaceKeybind(), click.button());

        startedDraggingInside = false;

        if (isInCircuit) {
            boolean isDestroy = !isPlace && matchesMouse(DefaultConfig.getConfig().getDestroyKeybind(), click.button());
            boolean isPick = !isDestroy && matchesMouse(DefaultConfig.getConfig().getPickKeybind(), click.button());

            if (isPlace) {
                ComponentState state = circuit.getComponentState(clickedPos);
                if (state.isAir()) {
                    placeComponent(clickedPos);
                    startedDraggingInside = true;
                } else {
                    circuit.useComponent(clickedPos, this.minecraft.player);
                }
                return true;
            }

            if (isDestroy) {
                breakComponent(clickedPos);
                startedDraggingInside = true;
                return true;
            }

            if (isPick) {
                ComponentState state = circuit.getComponentState(clickedPos);
                net.replaceitem.integratedcircuit.circuit.Component component = state.getComponent();
                selectPalette(component);

                return true;
            }
        } else {
            if (isPlace && Circuit.isPortPos(clickedPos)) {
                circuit.useComponent(clickedPos, this.minecraft.player);
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    private void breakComponent(ComponentPos pos) {
        circuit.breakComponentState(pos);
    }

    private void placeComponent(ComponentPos pos) {
        ComponentState state = circuit.getComponentState(pos);

        if (state.isAir() && this.cursorState != null) {
            circuit.placeComponentState(
                pos,
                this.cursorState.getComponent(),
                this.cursorRotation
            );
        }
    }

    private void selectPalette(net.replaceitem.integratedcircuit.circuit.Component component) {
        if(toolbox != null) {
            selectPalette(toolbox.getComponentIndex(component));
        }
    }

    private void selectPalette(int index) {
        if(toolbox != null) {
            toolbox.selectTool(index);
        }
    }

    private void deselectPalette() {
        if(toolbox != null) {
            toolbox.deselectTool();
        }
    }

    private void updateCursorState(net.replaceitem.integratedcircuit.circuit.@Nullable Component component) {
        if (component != null) {
            this.cursorState = component.getDefaultState();

            if (component instanceof FacingComponent) {
                this.cursorState = this.cursorState.setValue(
                    FacingComponent.FACING,
                    this.cursorRotation
                );
            }
        } else {
            this.cursorState = null;
        }
    }

    public void updateCustomNameForExternalChange(Component customName) {
        if (this.customNameTextField != null && !this.customNameTextField.isFocused()) {
            this.customNameTextField.setValue(customName.getString());
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (startedDraggingInside) {
            ComponentPos mousePos = getComponentPosAt((int) click.x(), (int) click.y());
            if (circuit.isInside(mousePos)) {
                boolean isPlace = matchesMouse(DefaultConfig.getConfig().getPlaceKeybind(), click.button());
                boolean isDestroy = !isPlace && matchesMouse(DefaultConfig.getConfig().getDestroyKeybind(), click.button());
                if (isPlace) {
                    placeComponent(mousePos);
                } else if (isDestroy) {
                    breakComponent(mousePos);
                }
                return true;
            }
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    private void rotateComponent(int amount) {
        if (this.cursorState != null && this.cursorState.getComponent() instanceof FacingComponent) {
            this.cursorRotation = this.cursorRotation.rotated(amount);
            this.cursorState = this.cursorState.setValue(FacingComponent.FACING, this.cursorRotation);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (DefaultConfig.getConfig().getInvertScrollDirection())
            verticalAmount = -verticalAmount;
        int intAmount = (int) verticalAmount;
        switch (DefaultConfig.getConfig().getScrollBehaviour()) {
            case ROTATE -> rotateComponent(-intAmount);
            case SELECT_COMPONENT -> {
                if(toolbox != null) {
                    selectPalette(toolbox.getSelectedToolSlot() - intAmount);
                }
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.customNameTextField != null && this.customNameTextField.isFocused()) {
            if (input.isConfirmation() || input.isEscape()) {
                customNameTextField.setFocused(false);
                return true;
            }
        }

        if (matchesKey(DefaultConfig.getConfig().getRotateKeybind(), input.input(), input.scancode())) {
            rotateComponent(1);
            return true;
        }

        if (input.input() >= GLFW.GLFW_KEY_0 && input.input() <= GLFW.GLFW_KEY_9) {
            if (input.input() == GLFW.GLFW_KEY_0) {
                deselectPalette();
            } else {
                selectPalette(input.input() - GLFW.GLFW_KEY_1);
            }
            return true;
        }
        return super.keyPressed(input);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    protected int getGridPosX(int gridX) {
        return this.x + GRID_X + gridX * RENDER_COMPONENT_SIZE;
    }

    protected int getGridPosY(int gridY) {
        return this.y + GRID_Y + gridY * RENDER_COMPONENT_SIZE;
    }

    protected int getGridXAt(int pixelX) {
        return Math.floorDiv(pixelX - this.x - GRID_X, RENDER_COMPONENT_SIZE);
    }

    protected int getGridYAt(int pixelY) {
        return Math.floorDiv(pixelY - this.y - GRID_Y, RENDER_COMPONENT_SIZE);
    }

    protected ComponentPos getComponentPosAt(int pixelX, int pixelY) {
        return new ComponentPos(getGridXAt(pixelX), getGridYAt(pixelY));
    }

    public static boolean matchesMouse(InputConstants.Key key, int button) {
        return key.getType() == InputConstants.Type.MOUSE && key.getValue() == button;
    }

    public static boolean matchesKey(InputConstants.Key key, int keyCode, int scanCode) {
        if (keyCode == InputConstants.UNKNOWN.getValue()) {
            return key.getType() == InputConstants.Type.SCANCODE && key.getValue() == scanCode;
        }
        return key.getType() == InputConstants.Type.KEYSYM && key.getValue() == keyCode;
    }

}
