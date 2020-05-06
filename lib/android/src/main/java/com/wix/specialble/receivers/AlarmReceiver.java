package com.wix.specialble.receivers;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.wix.specialble.BLEForegroundService;
import com.wix.specialble.util.CSVUtil;

/**
 * Created by hagai on 30/04/2020.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String WAKE_ME_UP = "wake.me.up";
    public static final String WAKE_ME_UP_AFTER_5 = "wake.me.up.after.5";
    public static final String WAKE_ME_UP_AFTER_10 = "wake.me.up.after.10";
    public static int alarmInterval = 1000 * 60 * 15;
    public static int alarmInterval_after_5 = 1000 * 60 * 5;
    public static int alarmInterval_after_10 = 1000 * 60 * 10;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction() != null ? intent.getAction() : WAKE_ME_UP;
        if(BleServiceRunning(context, BLEForegroundService.class))
        {
            //do nothing
        }
        else
        {
            BLEForegroundService.startThisService(context);
        }

        switch (action)
        {
            case WAKE_ME_UP:
                scheduleAlarms(context,WAKE_ME_UP,alarmInterval);
                break;
            case WAKE_ME_UP_AFTER_5:
                scheduleAlarms(context,WAKE_ME_UP_AFTER_5,alarmInterval);
                break;
            case WAKE_ME_UP_AFTER_10:
                scheduleAlarms(context,WAKE_ME_UP_AFTER_10,alarmInterval);
                break;
        }
    }

    public void scheduleAlarms(final Context ctx, final String action, final int delay)
    {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        alarmIntent.setAction(action);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
        }
        else
        {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
        }
    }

    private boolean BleServiceRunning(Context ctx, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
