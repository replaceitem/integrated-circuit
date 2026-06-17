package net.replaceitem.integratedcircuit.client.gui.widget;

import net.replaceitem.integratedcircuit.circuit.Component;
import org.jspecify.annotations.Nullable;

public record ToolSelectionInfo(int index, @Nullable Component component) {
}