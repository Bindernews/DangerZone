package com.vortexel.dangerzone.common;

import lombok.val;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public final class Reflector {

    private static Map<Class<?>, Map<String, Map<Integer, Method>>> methodCache = Maps.newIdentityHashMap();

    public static <T> T callMethod(Object obj, String name, Class<?>[] paramTypes, Object... params) {
        try {
            val m = obj.getClass().getMethod(name, paramTypes);
            methodCachePut(m);
            return invokeMethod(obj, m, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T callMethod(Object obj, String name, Object... params) {
        try {
            val m = findMethod(obj.getClass(), name, params.length);
            return invokeMethod(obj, m, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeMethod(Object obj, Method m, Object... params) {
        try {
            m.setAccessible(true);
            return (T)m.invoke(obj, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method findMethod(Class<?> clazz, String name, int paramCount) throws NoSuchMethodException {
        methodCacheEnsureExists(clazz, name);
        final Method cacheHit = methodCache.get(clazz).get(name).get(paramCount);
        if (cacheHit != null) {
            return cacheHit;
        }

        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                methodCachePut(m);
                return m;
            }
        }
        throw new NoSuchMethodException(String.format("No method %s in class %s with %d parameters",
                name, clazz.getName(), paramCount));
    }

    private static void methodCachePut(Method m) {
        methodCacheEnsureExists(m.getDeclaringClass(), m.getName());
        methodCache.get(m.getDeclaringClass()).get(m.getName()).put(m.getParameterCount(), m);
    }

    private static void methodCacheEnsureExists(Class<?> clazz, String name) {
        methodCache.putIfAbsent(clazz, Maps.newIdentityHashMap());
        methodCache.get(clazz).putIfAbsent(name, Maps.newHashMap());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object instance, Field field) {
        try {
            field.setAccessible(true);
            return (T)field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object obj, String name) {
        try {
            val f = obj.getClass().getField(name);
            f.setAccessible(true);
            return (T)f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void setField(Object obj, String name, T value) {
        try {
            val f = obj.getClass().getField(name);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void computeField(Object obj, String name, Function<T, T> func) {
        setField(obj, name, func.apply(getField(obj, name)));
    }

    // You won't make any instances of this class.
    private Reflector() {}
}
