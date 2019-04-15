package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Sets;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigHelper {

    public interface Loadable {
        void load(ConfigCategory category);
    }

    public interface FieldHandler {
        void handle(Object obj, Field field, ConfigCategory cfg) throws Exception;
    }

    private static class FHReg {
        public final Class<?> clazz;
        public final FieldHandler handler;

        public FHReg(Class<?> clazz, FieldHandler handler) {
            this.clazz = clazz;
            this.handler = handler;
        }
    }

    public static final Map<Class<?>, FieldHandler> FIELD_HANDLER_REGISTRY = Arrays.asList(
            new FHReg(int.class, ConfigHelper::handleInteger),
            new FHReg(int[].class, ConfigHelper::handleIntegerList),
            new FHReg(boolean.class, ConfigHelper::handleBoolean),
            new FHReg(boolean[].class, ConfigHelper::handleBooleanList),
            new FHReg(double.class, ConfigHelper::handleDouble),
            new FHReg(double[].class, ConfigHelper::handleDoubleList),
            new FHReg(String.class, ConfigHelper::handleString),
            new FHReg(String[].class, ConfigHelper::handleStringList)
    ).stream().collect(Collectors.toMap(r -> r.clazz, r -> r.handler));

    public static void loadAllCommented(Object obj, ConfigCategory cfg) {
        for (Field f : obj.getClass().getFields()) {
            if (f.getAnnotation(Config.Comment.class) != null) {
                readField(obj, f, cfg);
            }
        }
    }

    public static void readField(Object obj, Field field, ConfigCategory cfg) {
        FieldHandler handler = FIELD_HANDLER_REGISTRY.get(field.getType());
        if (handler != null) {
            try {
                handler.handle(obj, field, cfg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException("No handler for class " + field.getType().getName());
        }
    }

    public static String getComment(Class<?> clazz, String field) {
        try {
            return getComment(clazz.getField(field));
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static String getComment(Field field) {
        Config.Comment c = field.getAnnotation(Config.Comment.class);
        if (c == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = c.value();
        for (int i = 0; i < lines.length - 1; i++) {
            sb.append(lines[i]);
            sb.append("\n");
        }
        sb.append(lines[lines.length - 1]);
        return sb.toString();
    }

    /**
     * Searches {@param cfg} for categories in the form "[prefix].[integer]" and fills {@param initial}
     * with objects of type {@param clazz}.
     *
     * @param cfg
     * @param prefix
     * @param clazz
     * @param initial
     * @param <T>
     */
    public static <T extends Loadable> void loadIntegerMap(Configuration cfg, String prefix, Class<T> clazz,
                                                           Map<Integer, T> initial) {
        final String realPrefix = prefix + ".";

        // For each category name that starts with [prefix].
        for (String categoryName : Sets.filter(cfg.getCategoryNames(), (s) -> s.startsWith(realPrefix))) {
            String categoryIdSuffix = categoryName.substring(realPrefix.length());
            try {
                int keyId = Integer.parseInt(categoryIdSuffix);
                T value = construct(clazz);
                value.load(cfg.getCategory(categoryName));
                initial.put(keyId, value);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(String.format("Configuration key \"%s\" is not a number", categoryIdSuffix));
            }
        }
    }

    public static <T extends Loadable> void loadStringMap(Configuration cfg, String prefix, Class<T> clazz,
                                                          Map<String, T> initial) {
        final String realPrefix = prefix + ".";

        // For each category name that starts with [prefix].
        for (String categoryName : Sets.filter(cfg.getCategoryNames(), (s) -> s.startsWith(realPrefix))) {
            String keyId = categoryName.substring(realPrefix.length());
            T value = construct(clazz);
            value.load(cfg.getCategory(categoryName));
            initial.put(keyId, value);
        }
    }

    public static int[] integerToIntList(Collection<Integer> c) {
        int[] values = new int[c.size()];
        int i = 0;
        for (Integer v : c) {
            values[i] = v;
            i++;
        }
        return values;
    }

    public static List<Integer> intToIntegerList(int[] a) {
        List<Integer> values = new ArrayList<>(a.length);
        for (int v : a) {
            values.add(v);
        }
        return values;
    }

    public static String[] toStringArray(int[] a) {
        String[] out = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = Integer.toString(a[i]);
        }
        return out;
    }

    public static String[] toStringArray(double[] a) {
        String[] out = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = Double.toString(a[i]);
        }
        return out;
    }

    public static String[] toStringArray(boolean[] a) {
        String[] out = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = Boolean.toString(a[i]);
        }
        return out;
    }

    private static <T> T construct(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleInteger(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String defaultValue = Integer.toString(field.getInt(obj));
        Property prop = setupProperty(cfg, field, defaultValue, Property.Type.INTEGER);
        int value = prop.getInt();
        Config.RangeInt rangeInt = field.getAnnotation(Config.RangeInt.class);
        if (rangeInt != null) {
            if (value < rangeInt.min()) {
                value = rangeInt.min();
            }
            if (value > rangeInt.max()) {
                value = rangeInt.max();
            }
        }
        prop.set(value);
        field.setInt(obj, value);
    }


    private static void handleIntegerList(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String[] defaultValues = toStringArray((int[])field.get(obj));
        Property prop = setupProperty(cfg, field, defaultValues, Property.Type.INTEGER);
        int[] values = prop.getIntList();
        Config.RangeInt rangeInt = field.getAnnotation(Config.RangeInt.class);
        if (rangeInt != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] < rangeInt.min()) {
                    values[i] = rangeInt.min();
                }
                if (values[i] > rangeInt.max()) {
                    values[i] = rangeInt.max();
                }
            }
        }
        prop.set(values);
        field.set(obj, values);
    }

    private static void handleBoolean(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String defaultValue = Boolean.toString(field.getBoolean(obj));
        Property prop = setupProperty(cfg, field, defaultValue, Property.Type.BOOLEAN);
        boolean value = prop.getBoolean();
        prop.set(value);
        field.setBoolean(obj, value);
    }

    private static void handleBooleanList(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String[] defaultValues = toStringArray((boolean[])field.get(obj));
        Property prop = setupProperty(cfg, field, defaultValues, Property.Type.BOOLEAN);
        boolean[] values = prop.getBooleanList();
        prop.set(values);
        field.set(obj, values);
    }

    private static void handleDouble(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String defaultValue = Double.toString(field.getDouble(obj));
        Property prop = setupProperty(cfg, field, defaultValue, Property.Type.DOUBLE);
        double value = prop.getDouble(field.getDouble(obj));
        Config.RangeDouble rangeDouble = field.getAnnotation(Config.RangeDouble.class);
        if (rangeDouble != null) {
            if (value < rangeDouble.min()) {
                value = rangeDouble.min();
            }
            if (value > rangeDouble.max()) {
                value = rangeDouble.max();
            }
        }
        prop.set(value);
        field.setDouble(obj, value);
    }

    private static void handleDoubleList(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String[] defaultValues = toStringArray((double[])field.get(obj));
        Property prop = setupProperty(cfg, field, defaultValues, Property.Type.DOUBLE);
        double[] values = prop.getDoubleList();
        Config.RangeDouble rangeDouble = field.getAnnotation(Config.RangeDouble.class);
        if (rangeDouble != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] < rangeDouble.min()) {
                    values[i] = rangeDouble.min();
                }
                if (values[i] > rangeDouble.max()) {
                    values[i] = rangeDouble.max();
                }
            }
        }
        prop.set(values);
        field.set(obj, values);
    }

    private static void handleString(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String defaultValue = (String)field.get(obj);
        Property prop = setupProperty(cfg, field, defaultValue, Property.Type.STRING);
        String value = prop.getString();
        prop.set(value);
        field.set(obj, value);
    }

    private static void handleStringList(Object obj, Field field, ConfigCategory cfg) throws Exception {
        String[] defaultValues = (String[])field.get(obj);
        Property prop = setupProperty(cfg, field, defaultValues, Property.Type.STRING);
        String[] values = prop.getStringList();
        prop.set(values);
        field.set(obj, values);
    }

    public static Property setupProperty(ConfigCategory cfg, Field field, String value, Property.Type type) {
        final String name = field.getName();
        Property orElse = new Property(name, value, type);
        return setupProperty(cfg, field, orElse);
    }

    public static Property setupProperty(ConfigCategory cfg, Field field, String[] value, Property.Type type) {
        final String name = field.getName();
        Property orElse = new Property(name, value, type);
        return setupProperty(cfg, field, orElse);
    }

    public static Property setupProperty(ConfigCategory cfg, Field field, Property orElse) {
        Property prop = getProp(cfg, field.getName(), orElse);
        if (prop.getComment() == null || prop.getComment().equals("")) {
            prop.setComment(getComment(field));
        }
        return prop;
    }

    public static Property getProp(ConfigCategory cat, String name, Property orElse) {
        Property prop = cat.get(name);
        if (prop == null) {
            cat.put(name, orElse);
            prop = orElse;
        }
        return prop;
    }
}
