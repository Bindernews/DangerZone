package com.vortexel.dangerzone.common.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class JsonUtil {

    private static final String ERR_EXPECTED = "Expected \"$1%s\" to be $2%s but it was not!";

    //region getOrDefault

    public static float getOrDefault(JsonObject obj, String member, float defaultValue) {
        val sub = getPrimitive(obj, member);
        return sub != null ? sub.getAsFloat() : defaultValue;
    }

    public static boolean getOrDefault(JsonObject obj, String member, boolean defaultValue) {
        val sub = getPrimitive(obj, member);
        return sub != null ? sub.getAsBoolean() : defaultValue;
    }

    public static int getOrDefault(JsonObject obj, String member, int defaultValue) {
        val sub = getPrimitive(obj, member);
        return sub != null ? sub.getAsInt() : defaultValue;
    }

    public static String getOrDefault(JsonObject obj, String member, String defaultValue) {
        val sub = getPrimitive(obj, member);
        return sub != null ? sub.getAsString() : defaultValue;
    }

    public static JsonPrimitive getOrDefault(JsonObject obj, String member, JsonPrimitive defaultValue) {
        val sub = getPrimitive(obj, member);
        return sub != null ? sub : defaultValue;
    }

    //endregion getOrDefault

    //region getAssert



    //endregion getAssert

    public static JsonPrimitive getPrimitive(JsonObject obj, String member) {
        val sub = obj.get(member);
        return sub != null && sub.isJsonPrimitive() ? sub.getAsJsonPrimitive() : null;
    }

    public static List<String> readStringList(JsonObject obj, String member) {
        List<String> r = Lists.newArrayList();
        val sub = obj.get(member);
        if (sub == null) {
            return r;
        } else if (sub.isJsonPrimitive()) {
            r.add(sub.getAsString());
        } else if (sub.isJsonArray()) {
            val arr = sub.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                r.add(arr.get(i).getAsString());
            }
        } else if (sub.isJsonObject() || sub.isJsonNull()) {
            throw new UnsupportedOperationException(
                    String.format(ERR_EXPECTED, member, "a string or array of strings"));
        }
        return r;
    }

    /**
     * Returns a JsonArray of {@code values}.
     * @param values list of numeric values
     */
    public static JsonArray arrayOf(Number... values) {
        val arr = new JsonArray();
        for (val num : values) {
            arr.add(num);
        }
        return arr;
    }

    public static Pair<Float, Float> readPair(JsonObject obj, String field, float defaultMin, float defaultMax,
                                              SingleValue singleValueResponse) {
        val result = readPair(obj, field, singleValueResponse);
        return Pair.of(nullOrDefault(result.getLeft(), defaultMin), nullOrDefault(result.getRight(), defaultMax));
    }

    /**
     * Read {@code field} in {@code obj} as a pair of elements, or as a single value if only a primitive
     * value is supplied.
     *
     * @throws UnsupportedOperationException if the value isn't an array, the array contains more than two elements,
     *         or some other error parse error occurred.
     *
     * @param obj the {@link JsonObject} to read
     * @param field the name of the field to read
     * @param singleValueResponse how to handle if there's only a single primitive
     * @return a {@link Pair} of {@link JsonElement} objects, either or both of which may be {@code null} or
     *         {@link com.google.gson.JsonNull} if the value was not supplied or should be defaulted.
     */
    public static Pair<JsonElement, JsonElement> readPair(JsonObject obj, String field,
                                                          SingleValue singleValueResponse) {
        val errorMessage = "\"" + field + "\" must be an array with 2 elements";
        if (obj.has(field)) {
            val f = obj.get(field);
            JsonPrimitive primitive;
            if (f.isJsonPrimitive()) {
                primitive = f.getAsJsonPrimitive();
            } else if (f.isJsonArray()) {
                val arr = f.getAsJsonArray();
                switch (arr.size()) {
                    case 0:
                        return Pair.of(null, null);
                    case 1:
                        primitive = arr.get(0).getAsJsonPrimitive();
                        break;
                    case 2:
                        return Pair.of(arr.get(0), arr.get(1));
                    default:
                        throw new UnsupportedOperationException(errorMessage);
                }
            } else {
                throw new UnsupportedOperationException(errorMessage);
            }
            // If we reach this point, it was a single value.
            switch (singleValueResponse) {
                case LEFT:
                    return Pair.of(primitive, null);
                case RIGHT:
                    return Pair.of(null, primitive);
                case ERROR:
                    throw new UnsupportedOperationException(errorMessage);
                default:
                    throw new NullPointerException();
            }
        } else {
            return Pair.of(null, null);
        }
    }

    public static float nullOrDefault(JsonElement p, float defaultValue) {
        return (p == null || p.isJsonNull()) ? defaultValue : p.getAsFloat();
    }

    public enum SingleValue {
        LEFT,
        RIGHT,
        ERROR,
    }

    private JsonUtil() {}
}
