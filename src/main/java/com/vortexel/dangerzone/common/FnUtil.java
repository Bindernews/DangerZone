package com.vortexel.dangerzone.common;

public class FnUtil {

    public static <T> T orElse(T obj, T valueIfNull) {
        return obj == null ? valueIfNull : obj;
    }


    public static <T> T orElse(T obj, Factory<T> producerIfNull) {
        return obj == null ? producerIfNull.produce() : obj;
    }


    /**
     * A function that will produce a value of type {@code T}.
     * @param <T>
     */
    public interface Factory<T> {
        T produce();
    }
}
