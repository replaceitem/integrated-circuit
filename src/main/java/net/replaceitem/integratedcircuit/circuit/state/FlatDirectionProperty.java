package net.replaceitem.integratedcircuit.circuit.state;

import com.google.common.collect.Lists;
import net.minecraft.state.property.EnumProperty;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlatDirectionProperty extends EnumProperty<FlatDirection> {
    protected FlatDirectionProperty(String name, Collection<FlatDirection> values) {
        super(name, FlatDirection.class, values);
    }

    public static FlatDirectionProperty of(String name) {
        return of(name, flatDirection -> true);
    }

    public static FlatDirectionProperty of(String name, Predicate<FlatDirection> filter) {
        return of(name, Arrays.stream(FlatDirection.values()).filter(filter).collect(Collectors.toList()));
    }

    public static FlatDirectionProperty of(String name, FlatDirection... values) {
        return of(name, Lists.newArrayList(values));
    }

    public static FlatDirectionProperty of(String name, Collection<FlatDirection> values) {
        return new FlatDirectionProperty(name, values);
    }
}
