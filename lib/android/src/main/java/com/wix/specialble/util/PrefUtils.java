package com.wix.specialble.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PrefUtils {

    public static final String SERVICE_PREFS = "start_service_prefs";
    public static final String START_SERVICE_PREFS_KEY = "startServic";
    public static final String BATTERY_LEVEL_KEY = "battery_level";

    public static final void setStartServiceValue(Context context, boolean shouldStart) {

        SharedPreferences.Editor editor = context.getSharedPreferences(SERVICE_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(START_SERVICE_PREFS_KEY, shouldStart);
        editor.apply();
    }

    public static final boolean getStartServiceValue(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(SERVICE_PREFS, MODE_PRIVATE);
        return prefs.getBoolean(START_SERVICE_PREFS_KEY, false);
    }

    public static final void setCurrentBatteryLevel(Context context, int batteryLevel) {

        SharedPreferences.Editor editor = context.getSharedPreferences(SERVICE_PREFS, MODE_PRIVATE).edit();
        editor.putInt(BATTERY_LEVEL_KEY, batteryLevel);
        editor.apply();
    }

    public static final int getPreviousBatteryLevel(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(SERVICE_PREFS, MODE_PRIVATE);
        return prefs.getInt(BATTERY_LEVEL_KEY, -1);
    }
}
