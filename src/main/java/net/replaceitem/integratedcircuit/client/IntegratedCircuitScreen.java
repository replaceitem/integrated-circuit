package net.replaceitem.integratedcircuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.replaceitem.integratedcircuit.circuit.*;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.PortComponentState;
import net.replaceitem.integratedcircuit.circuit.state.RotatableComponentState;
import net.replaceitem.integratedcircuit.mixin.RedstoneWireBlockAccessor;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.network.packet.FinishEditingC2SPacket;
import net.replaceitem.integratedcircuit.util.SignalStrengthAccessor;
import org.lwjgl.glfw.GLFW;


@Environment(EnvType.CLIENT)
public class IntegratedCircuitScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new IntegratedCircuitIdentifier("textures/gui/integrated_circuit_screen.png");

    protected static final int BACKGROUND_WIDTH = 240;
    protected static final int BACKGROUND_HEIGHT = 230;
    
    public static final int COMPONENT_SIZE = 16;
    private static final int HALF_COMPONENT_SIZE = COMPONENT_SIZE/2;

    public static final int RENDER_COMPONENT_SIZE = 12;
    private static final int HALF_RENDER_COMPONENT_SIZE = RENDER_COMPONENT_SIZE/2;
    
    private static final float RENDER_SCALE = (((float)RENDER_COMPONENT_SIZE)/((float)COMPONENT_SIZE));

    private static final int PALETTE_X = 7;
    private static final int PALETTE_Y = 17;

    private static final int GRID_X = 40;
    private static final int GRID_Y = 30;

    protected int x, y;
    protected int titleX, titleY;

    protected final ClientCircuit circuit;
    protected final BlockPos pos;

    private int selectedComponentSlot = -1;
    private FlatDirection cursorRotation = FlatDirection.NORTH;
    private ComponentState cursorState = null;


    private static final Component[] PALETTE = new Component[]{
            Components.BLOCK,
            Components.WIRE,
            Components.TORCH,
            Components.REPEATER,
            Components.COMPARATOR,
            Components.OBSERVER,
            Components.TARGET,
            Components.REDSTONE_BLOCK
    };

    public IntegratedCircuitScreen(EditIntegratedCircuitS2CPacket packet) {
        super(packet.name);
        this.pos = packet.pos;
        this.circuit = packet.getClientCircuit();
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
        new FinishEditingC2SPacket(this.pos).send();
        super.close();
    }

    public ClientCircuit getClientCircuit() {
        return circuit;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        this.drawTexture(matrices, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        this.textRenderer.draw(matrices, this.title, this.titleX, this.titleY, 0x404040);
        
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        ComponentState componentState = circuit.getComponentState(pos);
        if(componentState instanceof SignalStrengthAccessor signalStrengthAccessor) {
            int signalStrength = signalStrengthAccessor.getSignalStrength();
            String signalStrengthString = String.valueOf(signalStrength);
            int signalStrengthStringLength = textRenderer.getWidth(signalStrengthString);
            Vec3d colorVec = RedstoneWireBlockAccessor.getCOLORS()[signalStrength].multiply(255);
            int color = ColorHelper.Argb.getArgb(0xFF, (int) colorVec.x, (int) colorVec.y, (int) colorVec.z);
            this.textRenderer.draw(matrices, Text.literal(signalStrengthString), this.x + BACKGROUND_WIDTH - 6 - signalStrengthStringLength,this.titleY, color);
        }

        this.renderContent(matrices);
        this.renderPalette(matrices);
        this.renderCursorState(matrices, mouseX, mouseY);
        

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderCursorState(MatrixStack matrices, int mouseX, int mouseY) {
        ComponentPos pos = getComponentPosAt(mouseX, mouseY);
        boolean validSpot = circuit.getComponentState(pos).isAir();
        float a = validSpot?0.7f:0.2f;
        if(this.cursorState != null) {
            matrices.push();
            matrices.translate(mouseX, mouseY, 0);
            matrices.scale(RENDER_SCALE, RENDER_SCALE, 1);
            renderComponentState(matrices, this.cursorState, -HALF_COMPONENT_SIZE, -HALF_COMPONENT_SIZE, a);
        }
    }

    private void renderPalette(MatrixStack matrices) {
        for (int i = 0; i < PALETTE.length; i++) {
            Component component = PALETTE[i];
            int slotY = this.getPaletteSlotPosY(i);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
            this.drawTexture(matrices, this.x + PALETTE_X, slotY, selectedComponentSlot == i ? 14 : 0, BACKGROUND_HEIGHT, 14, 14);
            Identifier itemTexture = component.getItemTexture();
            if(itemTexture != null) renderPaletteItem(matrices, itemTexture, this.x+PALETTE_X+1, slotY+1);
        }
    }
    
    private void renderPaletteItem(MatrixStack matrices, Identifier itemTexture, int x, int y) {
        prepareTextureRender(itemTexture, 1, 1, 1, 1);
        drawTexture(matrices, x, y, 0, 0, RENDER_COMPONENT_SIZE, RENDER_COMPONENT_SIZE, RENDER_COMPONENT_SIZE, RENDER_COMPONENT_SIZE);
    }

    protected void renderContent(MatrixStack matrices) {
        matrices.push();
        matrices.translate(getGridPosX(0), getGridPosY(0), 0);
        
        matrices.scale(RENDER_SCALE, RENDER_SCALE, 1);

        //matrices.translate(-getGridPosX(0), -getGridPosY(0), 0);
        
        for (int i = 0; i < circuit.ports.length; i++) {
            PortComponentState port = circuit.ports[i];
            ComponentPos pos = Circuit.PORTS_GRID_POS[i];
            renderComponentStateInGrid(matrices, port, pos.getX(), pos.getY());
        }
        for (int i = 0; i < circuit.components.length; i++) {
            ComponentState[] row = circuit.components[i];
            for (int j = 0; j < row.length; j++) {
                ComponentState componentState = row[j];
                renderComponentStateInGrid(matrices, componentState, i, j);
            }
        }
        
        matrices.pop();
    }

    protected static void renderComponentState(MatrixStack matrices, ComponentState state, int x, int y, float a) {
        state.getComponent().render(matrices, x, y, a, state);
    }

    protected void renderComponentStateInGrid(MatrixStack matrices, ComponentState state, int x, int y) {
        renderComponentState(matrices, state, x * COMPONENT_SIZE, y * COMPONENT_SIZE, 1);
    }


    public static void renderComponentTexture(MatrixStack matrices, Identifier component, int x, int y, int rot) {
        renderComponentTexture(matrices, component, x, y, rot, 1, 1, 1, 1);
    }

    public static void renderComponentTexture(MatrixStack matrices, Identifier component, int x, int y, int rot, float r, float g, float b, float a) {
        renderComponentTexture(matrices, component, x, y, rot, r, g, b, a, 0, 0, 16, 16);
    }

    public static void renderComponentTexture(MatrixStack matrices, Identifier component, int x, int y, int rot, float r, float g, float b, float a, int u, int v, int w, int h) {
        prepareTextureRender(component, r, g, b, a);
        matrices.push();
        matrices.translate(x+8, y+8, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (rot*Math.PI*0.5)));
        matrices.translate(-8, -8, 0);
        drawTexture(matrices, u, v, u, v, w, h, 16, 16);
        matrices.pop();
    }


    public static void renderComponentPart(MatrixStack matrices, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float r, float g, float b, float a) {
        renderComponentPart(matrices, texture, componentX, componentY, x, y, textureW, textureH, rot, r, g, b, a, 0, 0, textureW, textureH);
    }
    
    public static void renderComponentPart(MatrixStack matrices, Identifier texture, int componentX, int componentY, int x, int y, int textureW, int textureH, int rot, float r, float g, float b, float a, int u, int v, int w, int h) {
        prepareTextureRender(texture, r, g, b, a);
        matrices.push();
        matrices.translate(componentX+8, componentY+8, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (rot*Math.PI*0.5)));
        matrices.translate(-8, -8, 0);
        drawTexture(matrices, x, y, 0, 0, w, h, textureW, textureH);
        matrices.pop();
    }
    
    
    public static void prepareTextureRender(Identifier texture, float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(this.cursorState instanceof RotatableComponentState rotatableComponentState) {
            this.cursorRotation = this.cursorRotation.rotated(-((int) amount));
            rotatableComponentState.setRotation(this.cursorRotation);
            return true;
        }
        return false;
    }

    private void selectPalette(int slot) {
        if(slot < 0 || slot >= PALETTE.length) return;
        selectedComponentSlot = slot;
        this.cursorState = PALETTE[selectedComponentSlot].getDefaultState();
        if(this.cursorState instanceof RotatableComponentState rotatableComponentState) rotatableComponentState.setRotation(this.cursorRotation);
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
        ComponentPos clickedPos = getComponentPosAt((int) mouseX, (int) mouseY);
        
        if(circuit.isPort(clickedPos)) {
            circuit.cycleState(clickedPos, this.pos);
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
        
        if(circuit.isInside(clickedPos) && this.client != null) {
            boolean isUse = this.client.options.useKey.matchesMouse(button);
            boolean isAttack = client.options.attackKey.matchesMouse(button);
            boolean isPick = client.options.pickItemKey.matchesMouse(button);
            if(isUse) {
                ComponentState state = circuit.getComponentState(clickedPos);
                if(state.isAir()) {
                    if(this.cursorState == null) return false;
                    circuit.placeComponentState(clickedPos, this.cursorState.getComponent(), this.cursorRotation, this.pos);
                    return true;
                }
                circuit.cycleState(clickedPos, this.pos);
                return true;
            }
            if(isAttack) {
                circuit.breakComponentState(clickedPos, this.pos);
                return true;
            }
            if(isPick) {
                ComponentState state = circuit.getComponentState(clickedPos);
                Component component = state.getComponent();
                pickPalette(component);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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

    protected ComponentPos getComponentPosAt(int pixelX, int pixelY) {
        return new ComponentPos(getGridXAt(pixelX), getGridYAt(pixelY));
    }

    
}
