package com.example.mailit.prefrences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferencesManager {

    private static final String PREF_FILE_NAME = "com.example.mailit.SHARED_PREFRENCES";
    public static final String PREF_KEY_USER_ID = "USER_ID";
    public static final String PREF_KEY_FIRST_NAME = "FIRST_NAME";
    public static final String PREF_KEY_LAST_NAME = "LAST_NAME";
    public static final String PREF_KEY_EMAIL = "EMAIL";
    public static final String PREF_KEY_PASSWORD = "PASSWORD";
    public static final String PREF_KEY_PROFILE = "PROFILE";
    public static final String PREF_KEY_SESSION_TOKEN = "SESSION_TOKEN";
    public static final String PREF_KEY_IS_LOGIN = "IS_LOGIN";

    private static PreferencesManager sInstance;

    private final SharedPreferences mSharedPreferences;

    private PreferencesManager(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
        return sInstance;
    }

    public <T> void put(String key, T value) {

        if (value instanceof String) {
            mSharedPreferences.edit().putString(key, (String) value).apply();
            return;
        }

        if (value instanceof Integer) {
            mSharedPreferences.edit().putInt(key, (Integer) value).apply();
            return;
        }

        if (value instanceof Boolean) {
            mSharedPreferences.edit().putBoolean(key, (Boolean) value).apply();
            return;
        }

        if (value instanceof Float) {
            mSharedPreferences.edit().putFloat(key, (Float) value).apply();
            return;
        }

        if (value instanceof Long) {
            mSharedPreferences.edit().putLong(key, (Long) value).apply();
            return;
        }
    }

    public <T> T get(String key, T defaultValue) {
        if (defaultValue instanceof String) {
            return (T) mSharedPreferences.getString(key, (String) defaultValue);
        }

        if (defaultValue instanceof Integer) {
            Integer result = mSharedPreferences.getInt(key, (Integer) defaultValue);
            return (T) result;
        }

        if (defaultValue instanceof Boolean) {
            Boolean result = mSharedPreferences.getBoolean(key, (Boolean) defaultValue);
            return (T) result;
        }

        if (defaultValue instanceof Float) {
            Float result = mSharedPreferences.getFloat(key, (Float) defaultValue);
            return (T) result;
        }

        if (defaultValue instanceof Long) {
            Long result = mSharedPreferences.getLong(key, (Long) defaultValue);
            return (T) result;
        }
        return null;
    }
}
