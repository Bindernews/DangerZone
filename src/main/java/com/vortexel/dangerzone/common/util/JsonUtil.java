package com.vortexel.dangerzone.common.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.val;

public class JsonUtil {

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

    private JsonUtil() {}
}
