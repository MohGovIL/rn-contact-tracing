package moh.gov.il.specialble.sensor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import static android.content.Context.BATTERY_SERVICE;

public class SensorUtils {
    /**
     * Based on: https://developer.android.com/training/monitoring-device-state/battery-monitoring.html#MonitorLevel
     * And fixed float -> double casting based on: https://stackoverflow.com/questions/3291655/get-battery-level-and-state-in-android
     *
     * @param context A valid Android Context
     * @return Available battery in 0-100%.
     */
    public static int getBatteryPercentage(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter); //Note, we're passing a null receiver so we aren't really registering a receiver and don't need to unregister it.​
            //"LEVEL" is the existing status, "SCALE" is the max.
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;
            return (int) (batteryPct * 100);
        }
    }
}