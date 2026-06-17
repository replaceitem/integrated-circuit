package net.replaceitem.integratedcircuit.client.gui.widget;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Toolbox {
    private static final int PALETTE_COLS = 4;

    private final IntegratedCircuitScreen owner;
    private final int x;
    private final int y;

    public record PaletteEntry(Component component, Identifier itemTexture) {}

    private static final PaletteEntry[] PALETTE = new PaletteEntry[] {
        new PaletteEntry(Components.WIRE, IntegratedCircuit.id("toolbox/icons/redstone")),
        new PaletteEntry(Components.CROSSOVER, IntegratedCircuit.id("toolbox/icons/crossover")),
        new PaletteEntry(Components.TORCH, IntegratedCircuit.id("toolbox/icons/torch")),
        new PaletteEntry(Components.REDSTONE_BLOCK, IntegratedCircuit.id("toolbox/icons/redstone_block")),
        new PaletteEntry(Components.REPEATER, IntegratedCircuit.id("toolbox/icons/repeater")),
        new PaletteEntry(Components.COMPARATOR, IntegratedCircuit.id("toolbox/icons/comparator")),
        new PaletteEntry(Components.OBSERVER, IntegratedCircuit.id("toolbox/icons/observer")),
        new PaletteEntry(Components.BLOCK, IntegratedCircuit.id("toolbox/icons/block")),
        new PaletteEntry(Components.LAMP, IntegratedCircuit.id("toolbox/icons/lamp")),
        new PaletteEntry(Components.COPPER_BULB, IntegratedCircuit.id("toolbox/icons/copper_bulb")),
        new PaletteEntry(Components.LECTERN, IntegratedCircuit.id("toolbox/icons/lectern")),
        new PaletteEntry(Components.TARGET, IntegratedCircuit.id("toolbox/icons/target")),
        new PaletteEntry(Components.STONE_BUTTON, IntegratedCircuit.id("toolbox/icons/button_stone")),
        new PaletteEntry(Components.WOODEN_BUTTON, IntegratedCircuit.id("toolbox/icons/button_wood")),
        new PaletteEntry(Components.LEVER, IntegratedCircuit.id("toolbox/icons/lever"))
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
            var entry = PALETTE[i];

            int slotColumn = (i % PALETTE_COLS);
            int slotX =  slotColumn * ToolboxButton.SIZE;
            slotX += (ToolboxButton.MARGIN * (slotColumn + 1));

            int slotRow = (i / PALETTE_COLS);
            int slotY = slotRow * ToolboxButton.SIZE;
            slotY += (ToolboxButton.MARGIN * (slotRow + 1));

            final int index = i;

            componentButtons.add(new ToolboxButton(owner.getX() + this.x + slotX, owner.getY() + this.y + slotY, entry) {
                @Override
                public void onClick(MouseButtonEvent click, boolean doubled) {
                    if (selected) {
                        deselectTool();
                    } else {
                        selectTool(index);
                    }
                }
            });
        }

        componentButtons.forEach(owner::addRenderableWidget);
    }

    public void registerToolSelectionSubscriber(Consumer<ToolSelectionInfo> subscriber) {
        toolSelectionSubscribers.add(subscriber);
    }

    public void unregisterToolSelectionSubscriber(Consumer<ToolSelectionInfo> subscriber) {
        toolSelectionSubscribers.remove(subscriber);
    }

    @Nullable
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

    @Nullable
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
