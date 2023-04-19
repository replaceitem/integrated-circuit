package net.replaceitem.integratedcircuit.circuit;

import net.replaceitem.integratedcircuit.circuit.state.property.ComponentProperty;

import java.util.HashMap;

public class PropertyMap {
    private final HashMap<ComponentProperty<?>, Object> map;

    public PropertyMap() {
        this(new HashMap<>());
    }
    protected PropertyMap(HashMap<ComponentProperty<?>, Object> map) {
        this.map = map;
    }

    private <T> void putMutable(ComponentProperty<T> property, T value) {
        map.put(property, value);
    }

    public <T> void put(ComponentProperty<T> property, T value) {
        if(map.containsKey(property)) throw new RuntimeException("Tried modifying a PropertyMap");
        putMutable(property, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ComponentProperty<T> property) {
        return (T) map.get(property);
    }

    public byte encode() {
        byte b = 0;
        for (ComponentProperty<?> componentProperty : map.keySet()) {
            b = encodeProperty(componentProperty, b);
        }
        return b;
    }

    public <T> byte encodeProperty(ComponentProperty<T> property, byte data) {
        return property.write(data, get(property));
    }

    public <T> PropertyMap with(ComponentProperty<T> property, T value) {
        PropertyMap propertyMap = this.copy();
        propertyMap.putMutable(property, value);
        return propertyMap;
    }

    @SuppressWarnings("unchecked")
    protected PropertyMap copy() {
        return new PropertyMap((HashMap<ComponentProperty<?>, Object>) map.clone());
    }
}
