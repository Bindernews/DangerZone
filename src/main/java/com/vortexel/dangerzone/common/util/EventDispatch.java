package com.vortexel.dangerzone.common.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Consumer;

public class EventDispatch<T> {

    private int nextId;
    private Map<Consumer<T>, Integer> listeners;

    public EventDispatch() {
        listeners = Maps.newHashMap();
        nextId = 0;
    }

    public int add(Consumer<T> callback) {
        ++nextId;
        listeners.put(callback, nextId);
        return nextId;
    }

    public void remove(int callbackId) {
        Consumer<T> callback = mapGetKey(listeners, callbackId);
        remove(callback);
    }

    public void remove(Consumer<T> callback) {
        listeners.remove(callback);
    }

    public void fire(T value) {
        for (Consumer<T> callback : listeners.keySet()) {
            callback.accept(value);
        }
    }

    private static <K, V> K mapGetKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> e : map.entrySet()) {
            if (e.getValue().equals(value)) {
                return e.getKey();
            }
        }
        return null;
    }
}
