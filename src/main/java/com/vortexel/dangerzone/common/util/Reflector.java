package com.vortexel.dangerzone.common.util;

import lombok.val;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public final class Reflector {

    private static Map<Class<?>, Map<String, Map<Integer, Method>>> methodCache = Maps.newIdentityHashMap();


    public static <T> Method getMethod(Class<T> clazz, String name, Class<?>... paramTypes) {
        try {
            val m = clazz.getDeclaredMethod(name, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T callStaticMethod(Class<?> cls, String name, Class<?>[] paramTypes, Object... params) {
        val m = getMethod(cls, name, paramTypes);
        return callMethod(null, m, params);
    }

    public static <T> T callStaticMethod(Class<?> cls, String name, Object... params) {
        try {
            val m = findMethod(cls, name, params.length);
            return callMethod(null, m, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T callMethod(Object obj, String name, Class<?>[] paramTypes, Object... params) {
        val m = getMethod(obj.getClass(), name, paramTypes);
        methodCachePut(m);
        return callMethod(obj, m, params);
    }

    public static <T> T callMethod(Object obj, String name, Object... params) {
        try {
            val m = findMethod(obj.getClass(), name, params.length);
            return callMethod(obj, m, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethod(Object obj, Method m, Object... params) {
        try {
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

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                m.setAccessible(true);
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

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            val f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object instance, Field field) {
        try {
            return (T)field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object obj, String fieldName) {
        try {
            val f = getField(obj.getClass(), fieldName);
            return (T)f.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasField(Object instance, String name) {
        return hasField(instance.getClass(), name);
    }

    public static boolean hasField(Class<?> clazz, String name) {
        try {
            clazz.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public static <T> void set(Object obj, String fieldName, T value) {
        try {
            val f = getField(obj.getClass(), fieldName);
            f.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void set(Object obj, Field field, T value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void computeField(Object obj, String name, Function<T, T> func) {
        set(obj, name, func.apply(get(obj, name)));
    }

    // You won't make any instances of this class.
    private Reflector() {}
}
