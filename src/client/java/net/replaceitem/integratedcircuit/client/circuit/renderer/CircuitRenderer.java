package net.replaceitem.integratedcircuit.client.circuit.renderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.client.circuit.ClientCircuit;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

public class CircuitRenderer implements Renderable {
    private final ClientCircuit circuit;
    private final int x, y;
    private final int renderComponentSize;
    private final float renderScale;

    public static final int COMPONENT_SIZE = 16;

    public CircuitRenderer(ClientCircuit circuit, int x, int y, int renderComponentSize) {
        this.circuit = circuit;
        this.x = x;
        this.y = y;
        this.renderComponentSize = renderComponentSize;
        this.renderScale = ((float) renderComponentSize) / ((float) COMPONENT_SIZE);
    }

    private void inCircuitTransform(GuiGraphicsExtractor graphics, Runnable runnable) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(renderScale);

        runnable.run();

        graphics.pose().popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float dt) {
        inCircuitTransform(graphics, () -> {
            for (FlatDirection direction : FlatDirection.VALUES) {
                ComponentState port = circuit.getPorts()[direction.getIndex()];
                ComponentPos pos = Circuit.PORT_POSITIONS.get(direction);
                renderStateInTransformAtComponentPos(graphics, port, pos.getX(), pos.getY(), 1);
            }

            for (int i = 0; i < Circuit.SIZE; i++) {
                for (int j = 0; j < Circuit.SIZE; j++) {
                    ComponentState componentState = circuit.getSection().getComponentState(i, j);
                    renderStateInTransformAtComponentPos(graphics, componentState, i, j, 1);
                }
            }
        });
    }

    public record CircuitStatusTexts(@Nullable Component left, @Nullable Component right) {}
    public CircuitStatusTexts getStatusText(int mouseX, int mouseY) {
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

        var renderer = ComponentRenderers.get(component);
        if (renderer != null) {
            rightSideText = renderer.getHoverInfoText(componentState);
        }

        return new CircuitStatusTexts(leftSideText, rightSideText);
    }


    public int getGridPosX(int gridX) {
        return this.x + gridX * renderComponentSize;
    }

    public int getGridPosY(int gridY) {
        return this.y + gridY * renderComponentSize;
    }

    public int getGridXAt(int pixelX) {
        return Math.floorDiv(pixelX - this.x, renderComponentSize);
    }

    public int getGridYAt(int pixelY) {
        return Math.floorDiv(pixelY - this.y, renderComponentSize);
    }

    public ComponentPos getComponentPosAt(int pixelX, int pixelY) {
        return new ComponentPos(getGridXAt(pixelX), getGridYAt(pixelY));
    }

    private void renderStateInTransformAtComponentPos(GuiGraphicsExtractor graphics, ComponentState state, int x, int y, float a) {
        renderStateInTransformAt(graphics, state, x * COMPONENT_SIZE, y * COMPONENT_SIZE, a);
    }

    protected static void renderStateInTransformAt(GuiGraphicsExtractor graphics, ComponentState state, int x, int y, float a) {
        var renderer = ComponentRenderers.get(state.getComponent());
        if(renderer != null) {
            renderer.extractRenderState(graphics, x, y, a, state);
        }
    }

    public void renderStateAtComponentPos(GuiGraphicsExtractor graphics, ComponentState state, int x, int y, float a) {
        inCircuitTransform(graphics, () -> {
            renderStateInTransformAtComponentPos(graphics, state, x, y, a);
        });
    }

    public static Component getSignalStrengthText(int signalStrength) {
        int color = RedStoneWireBlock.getColorForPower(signalStrength);
        return Component.literal(String.valueOf(signalStrength)).withStyle(style -> style.withColor(color));
    }
}
