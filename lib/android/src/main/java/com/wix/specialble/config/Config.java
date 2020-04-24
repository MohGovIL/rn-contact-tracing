package com.wix.specialble.config;

import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;

public class Config  {

    private static final String PREF_NAME = "configPref";

    private static Config instance;
    private final SharedPreferences sharedPrefs;

    // Scanning Config - Default
    public static final long DEFAULT_SCAN_INTERVAL = 2 * 1000L;
    private static final long DEFAULT_SCAN_DURATION = 5 * 1000L;
    private static final int DEFAULT_SCAN_MODE = ScanSettings.SCAN_MODE_LOW_POWER; // enforced in background
    private static final int DEFAULT_SCAN_MATCH_MODE = ScanSettings.MATCH_MODE_AGGRESSIVE;

    // Scanning Config - Keys
    private static final String PREF_SCAN_INTERVAL = "scanningInterval";
    private static final String PREF_SCAN_DURATION = "scanDuration";
    private static final String PREF_SCAN_MODE = "scanMode";
    private static final String PREF_SCAN_MATCH_MODE = "scanMatchMode";


    // Advertising Config - Default
    public static final long DEFAULT_ADVERTISE_INTERVAL = 3 * 1000L; //5 * 60 * 1000L;
    private static final long DEFAULT_ADVERTISE_DURATION = 7 * 1000L;
    private static final int DEFAULT_ADVERTISE_TX_POWER_LEVEL = AdvertiseSettings.ADVERTISE_TX_POWER_LOW;
    private static final int DEFAULT_ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    // Advertising Config - Keys
    private static final String PREF_ADVERTISE_INTERVAL = "advertiseInterval";
    private static final String PREF_ADVERTISE_DURATION = "advertiseDuration";
    private static final String PREF_ADVERTISE_TX_POWER_LEVEL = "advertiseTXPowerLevel";
    private static final String PREF_ADVERTISE_MODE = "advertiseMode";

    // General Config - Default
    private static final String DEFAULT_SERVICEUUID = "00000000-0000-1000-8000-00805F9B34FB";
    private static final String DEFAULT_TOKEN = "default_public_key";
    private static final String DEFAULT_NOTIFICATION_TITLE = "BLE Contact Tracing";
    private static final String DEFAULT_NOTIFICATION_CONTENT = "Be Safe";

    // General Config - Keys
    private static final String PREF_SERVICEUUID = "serviceUUID";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_NOTIFICATION_TITLE = "notification_title";
    private static final String PREF_NOTIFICATION_CONTENT = "notification_content";



    public static synchronized Config getInstance(Context context) {
        if (instance == null) {
            instance = new Config(context);
        }
        return instance;
    }

    private Config(Context context) {
        sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setScanDuration(long scanDuration) {
        sharedPrefs.edit().putLong(PREF_SCAN_DURATION, scanDuration).apply();
    }

    public long getScanDuration() {
        return sharedPrefs.getLong(PREF_SCAN_DURATION, DEFAULT_SCAN_DURATION);
    }

    public void setScanInterval(long scanInterval) {
        sharedPrefs.edit().putLong(PREF_SCAN_INTERVAL, scanInterval).apply();
    }

    public long getScanInterval() {
        return sharedPrefs.getLong(PREF_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL);
    }


    public void setAdvertiseInterval(long scanInterval) {
        sharedPrefs.edit().putLong(PREF_ADVERTISE_INTERVAL, scanInterval).apply();
    }

    public long getAdvertiseInterval() {
        return sharedPrefs.getLong(PREF_ADVERTISE_INTERVAL, DEFAULT_ADVERTISE_INTERVAL);
    }

    public void setAdvertiseDuraton(long scanInterval) {
        sharedPrefs.edit().putLong(PREF_ADVERTISE_DURATION, scanInterval).apply();
    }

    public long getAdvertiseDuration() {
        return sharedPrefs.getLong(PREF_ADVERTISE_DURATION, DEFAULT_ADVERTISE_DURATION);
    }


    public void setAdvertiseTXPowerLevel(int powerLevel) {
        sharedPrefs.edit().putInt(PREF_ADVERTISE_TX_POWER_LEVEL, powerLevel).apply();
    }

    public int getAdvertiseTXPowerLevel() {
        return sharedPrefs.getInt(PREF_ADVERTISE_TX_POWER_LEVEL, DEFAULT_ADVERTISE_TX_POWER_LEVEL);
    }


    public void setAdvertiseMode(int advertiseMode) {
        sharedPrefs.edit().putInt(PREF_ADVERTISE_MODE, advertiseMode).apply();
    }

    public int getAdvertiseMode() {
        return sharedPrefs.getInt(PREF_ADVERTISE_MODE, DEFAULT_ADVERTISE_MODE);
    }


    public void setScanMode(int scanMode) {
        sharedPrefs.edit().putInt(PREF_ADVERTISE_MODE, scanMode).apply();
    }

    public int getScanMode() {
        return sharedPrefs.getInt(PREF_SCAN_MODE, DEFAULT_SCAN_MODE);
    }


    public void setServiceUUID(String serviceUUID) {
        sharedPrefs.edit().putString(PREF_SERVICEUUID, serviceUUID).apply();
    }

    public String getServiceUUID() {
        return sharedPrefs.getString(PREF_SERVICEUUID, DEFAULT_SERVICEUUID);
    }

    public void setToken(String token) {
        sharedPrefs.edit().putString(PREF_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPrefs.getString(PREF_TOKEN, DEFAULT_TOKEN);
    }

    public void setScanMatchMode(int scanMatchMode) {
        sharedPrefs.edit().putInt(PREF_SCAN_MATCH_MODE, scanMatchMode).apply();
    }

    public int getScanMatchMode() {
        return sharedPrefs.getInt(PREF_SCAN_MATCH_MODE, DEFAULT_SCAN_MATCH_MODE);
    }


    public void setNotificationTitle(String title) {
        sharedPrefs.edit().putString(PREF_NOTIFICATION_TITLE, title).apply();
    }

    public String getNotificationTitle() {
        return sharedPrefs.getString(PREF_NOTIFICATION_TITLE, DEFAULT_NOTIFICATION_TITLE);
    }

    public void setNotificationContent(String content) {
        sharedPrefs.edit().putString(PREF_NOTIFICATION_CONTENT, content).apply();
    }

    public String getNotificationContent() {
        return sharedPrefs.getString(PREF_NOTIFICATION_CONTENT, DEFAULT_NOTIFICATION_CONTENT);
    }

}
