package com.dudu.monitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.dudu.commonlib.CommonLib;

/**
 * SharedPreferences工具类
 */
public class SharedPreferencesUtil {

    public static SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(CommonLib.getInstance().getContext());
    }

    public static SharedPreferences getSharedPreferences(String name) {
        return CommonLib.getInstance().getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void putStringValue(String name, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(name).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void putStringValue(String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public static String getStringValue(String key, String defValue) {
        return  getDefaultSharedPreferences().getString(key, defValue);
    }

    public static void putLongValue(String key, long value) {
        SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLongValue(String key, long defValue) {
        return  getDefaultSharedPreferences().getLong(key,defValue);
    }

    public static String getStringValue(String name, String key, String defValue) {
        return getSharedPreferences(name).getString(key, defValue);
    }

    public static void putBooleanValue(String name, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(name).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void putBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBooleanValue(String key, boolean defValue) {
        return  getDefaultSharedPreferences().getBoolean(key, defValue);
    }

    public static boolean getBooleanValue(String name, String key, boolean defValue) {
        return getSharedPreferences(name).getBoolean(key, defValue);
    }

}
