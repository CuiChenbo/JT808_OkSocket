package com.cuichen.jt808_sdk.utils;

import android.preference.PreferenceManager;

import com.cuichen.jt808_sdk.sdk.SocketManager;

public final class PreferenceUtil {

    public static void set(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(SocketManager.getInstance().application).edit().putString(key, value).apply();
    }

    public static String get(String key) {
        return PreferenceManager.getDefaultSharedPreferences(SocketManager.getInstance().application).getString(key, null);
    }
    public static String get(String key, String value) {
        return PreferenceManager.getDefaultSharedPreferences(SocketManager.getInstance().application).getString(key, value);
    }

    public static void set(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(SocketManager.getInstance().application).edit().putBoolean(key, value).apply();
    }

    public static boolean get(String key, boolean value) {
        return PreferenceManager.getDefaultSharedPreferences(SocketManager.getInstance().application).getBoolean(key, value);
    }

    public static void clear(String key) {
        PreferenceManager.getDefaultSharedPreferences(SocketManager.getInstance().application).edit().remove(key).apply();
    }
}
