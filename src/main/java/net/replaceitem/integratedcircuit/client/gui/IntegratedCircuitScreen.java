package net.replaceitem.integratedcircuit.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

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

    protected Toolbox toolbox;
    protected TextFieldWidget customNameTextField;

    protected Text customName;
    protected final ClientCircuit circuit;

    private FlatDirection cursorRotation = FlatDirection.NORTH;
    private @Nullable ComponentState cursorState = null;

    public IntegratedCircuitScreen(ClientCircuit circuit, Text customName) {
        super(Text.translatable("integrated_circuit.gui.screen_title"));

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

        this.customNameTextField = new TextFieldWidget(
            this.textRenderer,
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

        this.customNameTextField.setDrawsBackground(false);
        this.customNameTextField.setMaxLength(50);
        this.customNameTextField.setEditable(true);
        this.customNameTextField.setText(this.customName.getString());
        this.customNameTextField.setPlaceholder(
            Text.translatable("integrated_circuit.gui.rename_field_placeholder")
                .styled(style ->
                        style.withColor(0x9C9C9C)
                             .withShadowColor(0x22222222))
        );
        this.addSelectableChild(this.customNameTextField);
        this.addDrawableChild(this.customNameTextField);
        this.customNameTextField.setChangedListener(this::setCustomName);
    }

    public void updateToolSelection(ToolSelectionInfo selectionInfo) {
        updateCursorState(selectionInfo.component());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        ClientPlayNetworking.send(new FinishEditingC2SPacket(this.circuit.getContext().getBlockPos()));
        super.close();
    }

    public ClientCircuit getClientCircuit() {
        return circuit;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        this.renderStatusBar(drawContext, mouseX, mouseY);
        this.renderContent(drawContext);
        this.renderCursorState(drawContext, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        context.drawTexture(
            RenderLayer::getGuiTextured,
            BACKGROUND_TEXTURE,
            x, y,
            0, 0,
            BACKGROUND_WIDTH,
            BACKGROUND_HEIGHT,
            512,
            512
        );
    }

    private void renderStatusBar(DrawContext drawContext, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        ComponentState componentState = circuit.getComponentState(pos);
        Component component = componentState.getComponent();

        int gridX = getGridXAt(mouseX);
        int gridY = getGridYAt(mouseY);

        Text leftSideText = null;
        Text rightSideText = null;

        if (circuit.isInside(pos)) {
            String componentName = component != Components.AIR
                ? component.getName().getString()
                : "";

            leftSideText = Text.literal(
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
            drawContext.drawText(
                this.textRenderer,
                leftSideText,
                this.x + STATUSBAR_X,
                this.y + STATUSBAR_Y,
                0x404040,
                false
            );
        }

        if (rightSideText != null) {
            int componentInfoWidth = this.textRenderer.getWidth(rightSideText);

            drawContext.drawText(
                this.textRenderer,
                rightSideText,
                this.x + BACKGROUND_WIDTH - componentInfoWidth - STATUSBAR_RIGHT_MARGIN,
                this.y + STATUSBAR_Y,
                0x404040,
                false
            );
        }
    }

    public static Text getSignalStrengthText(int signalStrength) {
        int color = RedstoneWireBlock.getWireColor(signalStrength);
        return Text.literal(String.valueOf(signalStrength)).styled(style -> style.withColor(color));
    }

    private void renderCursorState(DrawContext drawContext, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        boolean validSpot = circuit.getComponentState(pos).isAir();
        float a = validSpot ? 0.5f : 0.2f;
        if (this.cursorState != null && circuit.isInside(pos)) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(getGridPosX(0), getGridPosY(0), 0);
            drawContext.getMatrices().scale(RENDER_SCALE, RENDER_SCALE, 1);
            renderComponentStateInGrid(drawContext, this.cursorState, pos.getX(), pos.getY(), a);
            drawContext.getMatrices().pop();
        }
    }

    protected void renderContent(DrawContext drawContext) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(getGridPosX(0), getGridPosY(0), 0);

        drawContext.getMatrices().scale(RENDER_SCALE, RENDER_SCALE, 1);

        for (FlatDirection direction : FlatDirection.VALUES) {
            ComponentState port = circuit.getPorts()[direction.getIndex()];
            ComponentPos pos = Circuit.PORT_POSITIONS.get(direction);
            renderComponentStateInGrid(drawContext, port, pos.getX(), pos.getY(), 1);
        }

        for (int i = 0; i < Circuit.SIZE; i++) {
            for (int j = 0; j < Circuit.SIZE; j++) {
                ComponentState componentState = circuit.getSection().getComponentState(i, j);
                renderComponentStateInGrid(drawContext, componentState, i, j, 1);
            }
        }

        drawContext.getMatrices().pop();
    }

    protected static void renderComponentState(DrawContext drawContext, ComponentState state, int x, int y, float a) {
        state.getComponent().render(drawContext, x, y, a, state);
    }

    protected void renderComponentStateInGrid(DrawContext drawContext, ComponentState state, int x, int y, float a) {
        renderComponentState(drawContext, state, x * COMPONENT_SIZE, y * COMPONENT_SIZE, a);
    }

    public static void renderComponentTexture(DrawContext drawContext, Identifier component, int x, int y, int rot, float alpha) {
        renderComponentTexture(drawContext, component, x, y, rot, ColorHelper.getWhite(alpha));
    }

    public static void renderComponentTexture(DrawContext drawContext, Identifier component, int x, int y, int rot, int color) {
        renderComponentTexture(drawContext, component, x, y, rot, color, 0, 0, 16, 16);
    }

    public static void renderComponentTexture(DrawContext drawContext, Identifier component, int x, int y, int rot, int color, int u, int v, int w, int h) {
        renderPartialTexture(drawContext, component, x, y, u, v, 16, 16, rot, color, u, v, w, h);
    }


    public static void renderPartialTexture(DrawContext drawContext, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float alpha) {
        renderPartialTexture(drawContext, texture, componentX, componentY, x, y, textureW, textureH, rot, ColorHelper.getWhite(alpha));
    }

    public static void renderPartialTexture(DrawContext drawContext, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, int color) {
        renderPartialTexture(drawContext, texture, componentX, componentY, x, y, textureW, textureH, rot, color, 0, 0, textureW, textureH);
    }

    private static void renderPartialTexture(DrawContext drawContext, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, int color, int u, int v, int w, int h) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(componentX + 8, componentY + 8, 0);
        drawContext.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((float) (rot * Math.PI * 0.5)));
        drawContext.getMatrices().translate(-8, -8, 0);
        RenderSystem.enableBlend();
        drawContext.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u, v, w, h, textureW, textureH, color);
        drawContext.getMatrices().pop();
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.client == null)
            return false;

        ComponentPos clickedPos = getComponentPosAt((int) mouseX, (int) mouseY);

        if (customNameTextField.isFocused() && !customNameTextField.isMouseOver(mouseX, mouseY)) {
            customNameTextField.setFocused(false);
        }

        if (matchesMouse(DefaultConfig.config.getRotateKeybind(), button)) {
            rotateComponent(1);
            return true;
        }

        boolean isInCircuit = circuit.isInside(clickedPos);
        boolean isPlace = matchesMouse(DefaultConfig.config.getPlaceKeybind(), button);

        startedDraggingInside = false;

        if (isInCircuit) {
            boolean isDestroy = !isPlace && matchesMouse(DefaultConfig.config.getDestroyKeybind(), button);
            boolean isPick = !isDestroy && matchesMouse(DefaultConfig.config.getPickKeybind(), button);

            if (isPlace) {
                ComponentState state = circuit.getComponentState(clickedPos);
                if (state.isAir()) {
                    placeComponent(clickedPos);
                    startedDraggingInside = true;
                } else {
                    circuit.useComponent(clickedPos, this.client.player);
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
                Component component = state.getComponent();
                selectPalette(component);

                return true;
            }
        } else {
            if (isPlace && Circuit.isPortPos(clickedPos)) {
                circuit.useComponent(clickedPos, this.client.player);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

    private void selectPalette(Component component) {
        selectPalette(toolbox.getComponentIndex(component));
    }

    private void selectPalette(int index) {
        toolbox.selectTool(index);
    }

    private void deselectPalette() {
        toolbox.deselectTool();
    }

    private void setCustomName(String customName) {
        this.customName = Text.literal(customName);
    }

    private void updateCursorState(@Nullable Component component) {
        if (component != null) {
            this.cursorState = component.getDefaultState();

            if (component instanceof FacingComponent) {
                this.cursorState = this.cursorState.with(
                    FacingComponent.FACING,
                    this.cursorRotation
                );
            }
        } else {
            this.cursorState = null;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (startedDraggingInside && this.client != null) {
            ComponentPos mousePos = getComponentPosAt((int) mouseX, (int) mouseY);
            if (circuit.isInside(mousePos)) {
                boolean isPlace = matchesMouse(DefaultConfig.config.getPlaceKeybind(), button);
                boolean isDestroy = !isPlace && matchesMouse(DefaultConfig.config.getDestroyKeybind(), button);
                if (isPlace) {
                    placeComponent(mousePos);
                } else if (isDestroy) {
                    breakComponent(mousePos);
                }
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void rotateComponent(int amount) {
        if (this.cursorState != null && this.cursorState.getComponent() instanceof FacingComponent) {
            this.cursorRotation = this.cursorRotation.rotated(amount);
            this.cursorState = this.cursorState.with(FacingComponent.FACING, this.cursorRotation);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (DefaultConfig.config.getInvertScrollDirection())
            verticalAmount = -verticalAmount;
        int intAmount = (int) verticalAmount;
        switch (DefaultConfig.config.getScrollBehaviour()) {
            case ROTATE -> rotateComponent(-intAmount);
            case SELECT_COMPONENT ->
                selectPalette(toolbox.getSelectedToolSlot() - intAmount);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.customNameTextField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                customNameTextField.setFocused(false);
                return true;
            }
        }

        if (matchesKey(DefaultConfig.config.getRotateKeybind(), keyCode, scanCode)) {
            rotateComponent(1);
            return true;
        }

        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            if (keyCode == GLFW.GLFW_KEY_0) {
                deselectPalette();
            } else {
                selectPalette(keyCode - GLFW.GLFW_KEY_1);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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

    public static boolean matchesMouse(InputUtil.Key key, int button) {
        return key.getCategory() == InputUtil.Type.MOUSE && key.getCode() == button;
    }

    public static boolean matchesKey(InputUtil.Key key, int keyCode, int scanCode) {
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            return key.getCategory() == InputUtil.Type.SCANCODE && key.getCode() == scanCode;
        }
        return key.getCategory() == InputUtil.Type.KEYSYM && key.getCode() == keyCode;
    }

}
