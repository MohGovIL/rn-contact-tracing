package com.wix.specialble.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PrefUtils {

    public static final String START_SERVICE_PREFS = "start_service_prefs";
    public static final String START_SERVICE_PREFS_KEY = "startServic";

    public static final void setStartServiceValue(Context context, boolean shouldStart) {

        SharedPreferences.Editor editor = context.getSharedPreferences(START_SERVICE_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean("START_SERVICE_PREFS_KEY", true);
        editor.apply();
    }

    public static final boolean getStartServiceValue(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(START_SERVICE_PREFS, MODE_PRIVATE);
        boolean startService = prefs.getBoolean(START_SERVICE_PREFS_KEY, false);
        return startService;
    }
}
