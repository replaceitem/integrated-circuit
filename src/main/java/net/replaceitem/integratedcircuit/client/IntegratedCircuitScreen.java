package net.replaceitem.integratedcircuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ClientCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.components.FacingComponent;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


@Environment(EnvType.CLIENT)
public class IntegratedCircuitScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new IntegratedCircuitIdentifier("textures/gui/integrated_circuit_screen.png");

    protected static final int BACKGROUND_WIDTH = 240;
    protected static final int BACKGROUND_HEIGHT = 230;
    
    public static final int COMPONENT_SIZE = 16;

    public static final int RENDER_COMPONENT_SIZE = 12;
    
    private static final float RENDER_SCALE = (((float)RENDER_COMPONENT_SIZE)/((float)COMPONENT_SIZE));

    private static final int PALETTE_X = 7;
    private static final int PALETTE_Y = 17;

    private static final int GRID_X = 40;
    private static final int GRID_Y = 30;

    protected int x, y;
    protected int titleX, titleY;

    protected final ClientCircuit circuit;

    private int selectedComponentSlot = -1;
    private FlatDirection cursorRotation = FlatDirection.NORTH;
    
    @Nullable
    private ComponentState cursorState = null;


    private static final Component[] PALETTE = new Component[]{
            Components.BLOCK,
            Components.WIRE,
            Components.CROSSOVER,
            Components.TORCH,
            Components.REPEATER,
            Components.COMPARATOR,
            Components.OBSERVER,
            Components.TARGET,
            Components.REDSTONE_BLOCK,
            Components.LAMP,
            Components.LEVER,
            Components.STONE_BUTTON,
            Components.WOODEN_BUTTON
    };

    public IntegratedCircuitScreen(ClientCircuit circuit, Text name) {
        super(name);
        this.circuit = circuit;
    }

    @Override
    protected void init() {
        this.x = (this.width - BACKGROUND_WIDTH) / 2;
        this.y = (this.height - BACKGROUND_HEIGHT) / 2;
        this.titleX = this.x + 8;
        this.titleY = this.y + 6;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        new FinishEditingC2SPacket(this.circuit.getBlockPos()).send();
        super.close();
    }

    public ClientCircuit getClientCircuit() {
        return circuit;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        drawContext.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        drawContext.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);

        this.renderHoverInfo(drawContext, mouseX, mouseY);
        this.renderContent(drawContext);
        this.renderPalette(drawContext);
        this.renderCursorState(drawContext, mouseX, mouseY);
        

        super.render(drawContext, mouseX, mouseY, delta);
    }

    private void renderHoverInfo(DrawContext drawContext, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        ComponentState componentState = circuit.getComponentState(pos);
        Text text = componentState.getHoverInfoText();
        int textWidth = textRenderer.getWidth(text);
        drawContext.drawText(this.textRenderer, text, this.x + BACKGROUND_WIDTH - 6 - textWidth,this.titleY, 0x404040, false);
    }

    public static Text getSignalStrengthText(int signalStrength) {
        Vec3d colorVec = RedstoneWireBlockAccessor.getCOLORS()[signalStrength].multiply(255);
        int color = ColorHelper.Argb.getArgb(0xFF, (int) colorVec.x, (int) colorVec.y, (int) colorVec.z);
        return Text.literal(String.valueOf(signalStrength)).styled(style -> style.withColor(color));
    }

    private void renderCursorState(DrawContext drawContext, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        boolean validSpot = circuit.getComponentState(pos).isAir();
        float a = validSpot?0.5f:0.2f;
        if(this.cursorState != null && circuit.isInside(pos)) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(getGridPosX(0), getGridPosY(0), 0);
            drawContext.getMatrices().scale(RENDER_SCALE, RENDER_SCALE, 1);
            renderComponentStateInGrid(drawContext, this.cursorState, pos.getX(), pos.getY(), a);
            drawContext.getMatrices().pop();
        }
    }

    private void renderPalette(DrawContext drawContext) {
        for (int i = 0; i < PALETTE.length; i++) {
            Component component = PALETTE[i];
            int slotY = this.getPaletteSlotPosY(i);
            drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            drawContext.drawTexture(BACKGROUND_TEXTURE, this.x + PALETTE_X, slotY, selectedComponentSlot == i ? 14 : 0, BACKGROUND_HEIGHT, 14, 14);
            Identifier itemTexture = component.getItemTexture();
            if(itemTexture != null) renderPaletteItem(drawContext, itemTexture, this.x+PALETTE_X+1, slotY+1);
        }
    }
    
    private void renderPaletteItem(DrawContext drawContext, Identifier itemTexture, int x, int y) {
        drawContext.setShaderColor(1,1,1,1);
        drawContext.drawTexture(itemTexture, x, y, 0, 0, RENDER_COMPONENT_SIZE, RENDER_COMPONENT_SIZE, RENDER_COMPONENT_SIZE, RENDER_COMPONENT_SIZE);
    }

    protected void renderContent(DrawContext drawContext) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(getGridPosX(0), getGridPosY(0), 0);
        
        drawContext.getMatrices().scale(RENDER_SCALE, RENDER_SCALE, 1);
        
        for (int i = 0; i < circuit.ports.length; i++) {
            ComponentState port = circuit.ports[i];
            ComponentPos pos = Circuit.PORTS_GRID_POS[i];
            renderComponentStateInGrid(drawContext, port, pos.getX(), pos.getY(), 1);
        }
        for (int i = 0; i < circuit.components.length; i++) {
            ComponentState[] row = circuit.components[i];
            for (int j = 0; j < row.length; j++) {
                ComponentState componentState = row[j];
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


    private static void renderComponentTexture(DrawContext drawContext, Identifier component, int x, int y, int rot) {
        renderComponentTexture(drawContext, component, x, y, rot, 1, 1, 1, 1);
    }

    public static void renderComponentTexture(DrawContext drawContext, Identifier component, int x, int y, int rot, float r, float g, float b, float a) {
        renderComponentTexture(drawContext, component, x, y, rot, r, g, b, a, 0, 0, 16, 16);
    }

    public static void renderComponentTexture(DrawContext drawContext, Identifier component, int x, int y, int rot, float r, float g, float b, float a, int u, int v, int w, int h) {
        renderPartialTexture(drawContext, component, x, y, u, v, 16, 16, rot, r, g, b, a, u, v, w, h);
    }


    public static void renderPartialTexture(DrawContext drawContext, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float r, float g, float b, float a) {
        renderPartialTexture(drawContext, texture, componentX, componentY, x, y, textureW, textureH, rot, r, g, b, a, 0, 0, textureW, textureH);
    }

    private static void renderPartialTexture(DrawContext drawContext, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float r, float g, float b, float a, int u, int v, int w, int h) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(componentX+8, componentY+8, 0);
        drawContext.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((float) (rot*Math.PI*0.5)));
        drawContext.getMatrices().translate(-8, -8, 0);
        RenderSystem.enableBlend();
        drawContext.setShaderColor(r, g, b, a);
        drawContext.drawTexture(texture, x, y, u, v, w, h, textureW, textureH);
        drawContext.getMatrices().pop();
    }

    private void selectPalette(int slot) {
        if(slot < 0 || slot >= PALETTE.length) return;
        selectedComponentSlot = slot;
        this.cursorState = PALETTE[selectedComponentSlot].getDefaultState();
        if(this.cursorState.getComponent() instanceof FacingComponent) this.cursorState = this.cursorState.with(FacingComponent.FACING, this.cursorRotation);
    }

    private void deselectPalette() {
        selectedComponentSlot = -1;
        this.cursorState = null;
    }
    
    private void pickPalette(Component component) {
        for (int i = 0; i < PALETTE.length; i++) {
            if(PALETTE[i] == component) {
                selectPalette(i);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.client == null) return false;
        ComponentPos clickedPos = getComponentPosAt((int) mouseX, (int) mouseY);


        if(matchesMouse(DefaultConfig.config.getRotateKeybind(), button)) {
            rotateComponent(1);
            return true;
        }
        
        
        boolean isPlace = matchesMouse(DefaultConfig.config.getPlaceKeybind(), button);
        
        boolean isInCircuit = circuit.isInside(clickedPos);
        startedDraggingInside = false;
        if(isInCircuit) {
            boolean isDestroy = !isPlace && matchesMouse(DefaultConfig.config.getDestroyKeybind(), button);
            boolean isPick = !isDestroy &&  matchesMouse(DefaultConfig.config.getPickKeybind(), button);

            if(isPlace) {
                ComponentState state = circuit.getComponentState(clickedPos);
                if(state.isAir()) {
                    placeComponent(clickedPos);
                    startedDraggingInside = true;
                } else {
                    circuit.useComponent(clickedPos, this.client.player);
                }
                return true;
            }
            if(isDestroy) {
                breakComponent(clickedPos);
                startedDraggingInside = true;
                return true;
            }
            if(isPick) {
                ComponentState state = circuit.getComponentState(clickedPos);
                Component component = state.getComponent();
                pickPalette(component);
                return true;
            }
        } else {
            if(isPlace && circuit.isPortPos(clickedPos)) {
                circuit.useComponent(clickedPos, this.client.player);
                return true;
            }
            
            if(mouseX >= this.x+PALETTE_X && mouseX < this.x+PALETTE_X+14) {
                int slot = getPaletteSlotAt((int) mouseY);
                if(slot >= 0 && slot < PALETTE.length) {
                    if(selectedComponentSlot != slot) {
                        selectPalette(slot);
                    } else {
                        deselectPalette();
                    }
                    return true;
                }
            }
        }
        
        
        


        

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean startedDraggingInside = false;

    private void breakComponent(ComponentPos pos) {
        circuit.breakComponentState(pos);
    }

    private void placeComponent(ComponentPos pos) {
        ComponentState state = circuit.getComponentState(pos);
        if(state.isAir() && this.cursorState != null) {
            circuit.placeComponentState(pos, this.cursorState.getComponent(), this.cursorRotation);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(startedDraggingInside && this.client != null) {
            ComponentPos mousePos = getComponentPosAt((int) mouseX, (int) mouseY);
            if(circuit.isInside(mousePos)) {
                boolean isPlace = matchesMouse(DefaultConfig.config.getPlaceKeybind(), button);
                boolean isDestroy = !isPlace && matchesMouse(DefaultConfig.config.getDestroyKeybind(), button);
                if(isPlace) {
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
        if(this.cursorState != null && this.cursorState.getComponent() instanceof FacingComponent) {
            this.cursorRotation = this.cursorRotation.rotated(amount);
            this.cursorState = this.cursorState.with(FacingComponent.FACING, this.cursorRotation);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(DefaultConfig.config.getInvertScrollDirection()) amount = -amount;
        int intAmount = (int) amount;
        switch (DefaultConfig.config.getScrollBehaviour()) {
            case ROTATE -> rotateComponent(-intAmount);
            case SELECT_COMPONENT -> selectPalette(selectedComponentSlot - intAmount);
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(matchesKey(DefaultConfig.config.getRotateKeybind(), keyCode, scanCode)) {
            rotateComponent(1);
            return true;
        }
        
        
        if(keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            if(keyCode == GLFW.GLFW_KEY_0) {
                deselectPalette();
            } else {
                selectPalette(keyCode - GLFW.GLFW_KEY_1);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    protected int getPaletteSlotPosY(int slot) {
        return this.y+PALETTE_Y + slot*14;
    }

    protected int getPaletteSlotAt(int posY) {
        return (posY-this.y-PALETTE_Y)/14;
    }
    protected int getGridPosX(int gridX) {
        return this.x + GRID_X + gridX*RENDER_COMPONENT_SIZE;
    }

    protected int getGridPosY(int gridY) {
        return this.y + GRID_Y + gridY*RENDER_COMPONENT_SIZE;
    }
    protected int getGridXAt(int pixelX) {
        return Math.floorDiv(pixelX-this.x-GRID_X, RENDER_COMPONENT_SIZE);
    }

    protected int getGridYAt(int pixelY) {
        return Math.floorDiv(pixelY-this.y-GRID_Y, RENDER_COMPONENT_SIZE);
    }

    protected int getGridPosX(ComponentPos pos) {
        return getGridPosX(pos.getX());
    }
    protected int getGridPosY(ComponentPos pos) {
        return getGridPosY(pos.getY());
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
