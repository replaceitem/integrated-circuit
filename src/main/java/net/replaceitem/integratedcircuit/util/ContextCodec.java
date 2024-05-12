package net.replaceitem.integratedcircuit.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

@FunctionalInterface
public interface ContextCodec<C, A> {
    Codec<A> withContext(C context);
    
    default <T> DataResult<A> parse(C context, final DynamicOps<T> ops, final T input) {
        return withContext(context).decode(ops, input).map(Pair::getFirst);
    }

    default <T> DataResult<T> encodeStart(C context, final DynamicOps<T> ops, final A input) {
        return withContext(context).encode(input, ops, ops.empty());
    }
}
