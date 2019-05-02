package com.vortexel.dangerzone.common.util;

import java.util.function.Supplier;

public class FnUtil {

    public static <T> T orElse(T obj, T valueIfNull) {
        return obj == null ? valueIfNull : obj;
    }


    public static <T> T orElse(T obj, Supplier<T> producerIfNull) {
        return obj == null ? producerIfNull.get() : obj;
    }
}
