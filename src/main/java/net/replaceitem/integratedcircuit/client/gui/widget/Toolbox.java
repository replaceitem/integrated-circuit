package net.replaceitem.integratedcircuit.client.gui.widget;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Toolbox {
    private static final int PALETTE_COLS = 4;

    private final IntegratedCircuitScreen owner;
    private final int x;
    private final int y;

    private static final Component[] PALETTE = new Component[] {
        Components.WIRE,
        Components.CROSSOVER,
        Components.TORCH,
        Components.REDSTONE_BLOCK,
        Components.REPEATER,
        Components.COMPARATOR,
        Components.OBSERVER,
        Components.BLOCK,
        Components.LAMP,
        Components.COPPER_BULB,
        Components.LECTERN,
        Components.TARGET,
        Components.STONE_BUTTON,
        Components.WOODEN_BUTTON,
        Components.LEVER
    };

    private int selectedToolSlot = -1;
    private final List<ToolboxButton> componentButtons = new ArrayList<>(PALETTE.length);
    private final List<Consumer<ToolSelectionInfo>> toolSelectionSubscribers = new ArrayList<>();

    public Toolbox(IntegratedCircuitScreen owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;
    }

    public void init() {
        componentButtons.clear();

        for (int i = 0; i < PALETTE.length; i++) {
            Component component = PALETTE[i];

            int slotColumn = (i % PALETTE_COLS);
            int slotX =  slotColumn * ToolboxButton.SIZE;
            slotX += (ToolboxButton.MARGIN * (slotColumn + 1));

            int slotRow = (i / PALETTE_COLS);
            int slotY = slotRow * ToolboxButton.SIZE;
            slotY += (ToolboxButton.MARGIN * (slotRow + 1));

            final int index = i;

            componentButtons.add(new ToolboxButton(owner.getX() + this.x + slotX, owner.getY() + this.y + slotY, component) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    if (selected) {
                        deselectTool();
                    } else {
                        selectTool(index);
                    }
                }
            });
        }

        componentButtons.forEach(owner::addDrawableChild);
    }

    public void registerToolSelectionSubscriber(Consumer<ToolSelectionInfo> subscriber) {
        toolSelectionSubscribers.add(subscriber);
    }

    public void unregisterToolSelectionSubscriber(Consumer<ToolSelectionInfo> subscriber) {
        toolSelectionSubscribers.remove(subscriber);
    }

    public ToolSelectionInfo selectTool(int index) {
        if (index < 0 || index >= componentButtons.size())
            return null;

        if (selectedToolSlot >= 0 && selectedToolSlot < componentButtons.size())
            componentButtons.get(selectedToolSlot).setSelected(false);

        selectedToolSlot = index;
        ToolboxButton toolboxButton = componentButtons.get(index);
        toolboxButton.setSelected(true);

        Component component = toolboxButton.getComponent();

        ToolSelectionInfo selectionInfo = new ToolSelectionInfo(index, component);
        notifyToolSelectionSubscribers(selectionInfo);

        return selectionInfo;
    }

    public void deselectTool() {
        if (selectedToolSlot >= 0 && selectedToolSlot < componentButtons.size()) {
            componentButtons.get(selectedToolSlot).setSelected(false);
        }

        selectedToolSlot = -1;

        notifyToolSelectionSubscribers(
            new ToolSelectionInfo(
                selectedToolSlot,
                null
            )
        );
    }

    private void notifyToolSelectionSubscribers(ToolSelectionInfo selectionInfo) {
        for (Consumer<ToolSelectionInfo> subscriber : toolSelectionSubscribers) {
            subscriber.accept(selectionInfo);
        }
    }

    public Component getComponent(int index) {
        if (index < 0 || index >= componentButtons.size())
            return null;

        return componentButtons.get(index).getComponent();
    }

    public int getComponentIndex(Component component) {
        for (int i = 0; i < componentButtons.size(); i++) {
            if (componentButtons.get(i).getComponent() == component) {
                return i;
            }
        }

        return -1;
    }

    public int getSelectedToolSlot() {
        return selectedToolSlot;
    }
}
