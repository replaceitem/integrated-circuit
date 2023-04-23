package net.replaceitem.integratedcircuit.circuit.state;

import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.PropertyMap;
import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractComponentState {
    protected PropertyMap propertyMap;
    protected final Component component;

    public static class PropertyBuilder {
        private final Set<ComponentProperty<?>> properties = new HashSet<>();

        public void append(ComponentProperty<?>... properties) {
            this.properties.addAll(Arrays.asList(properties));
        }
        public Set<ComponentProperty<?>> getProperties() {
            return properties;
        }
    }

    public AbstractComponentState(byte data, Component component) {
        this.component = component;
        this.propertyMap = new PropertyMap();
        for (ComponentProperty<?> property : getProperties()) {
            property.readIntoMap(this.propertyMap, data);
        }
    }

    protected abstract Set<ComponentProperty<?>> getProperties();
    protected abstract int getComponentId();


    public byte encodeStateData() {
        return propertyMap.encode();
    }

    public short encode() {
        return (short) (this.encodeStateData() << 8 | getComponentId() & 0xFF);
    }
}
