package com.cuichen.jt808_oksocket.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.List;

public class GsonUtils {

    static volatile Gson gson;

    static {
        if (null == gson) {
            gson = new Gson();
        }
    }

    public static boolean isJson(String jsonStr){
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(jsonStr);
        } catch (Exception e) {
            return false;
        }
        if (jsonElement == null) {
            return false;
        }
        if (!jsonElement.isJsonObject()) {
            return false;
        }
        return true;
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        synchronized (GsonUtils.class) {
            return gson.fromJson(json, cls);
        }
    }

    public static <T> T fromJson(JsonElement json, Class<T> cls) {
        synchronized (GsonUtils.class) {
            return gson.fromJson(json, cls);
        }
    }

    public static <T> T fromArray(String json, Type typeOfT) {
        synchronized (GsonUtils.class) {
            return gson.fromJson(json, typeOfT);
        }
    }

    public static <T> T fromArray(JsonElement json, Type typeOfT) {
        synchronized (GsonUtils.class) {
            return gson.fromJson(json, typeOfT);
        }
    }

    public static <T> String toJson(T t) {
        synchronized (GsonUtils.class) {
            return gson.toJson(t);
        }
    }

    public static <T> String toArray(List<T> list) {
        synchronized (GsonUtils.class) {
            return gson.toJson(list);
        }
    }
}
