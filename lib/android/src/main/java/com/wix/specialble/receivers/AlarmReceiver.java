package com.wix.specialble.receivers;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    private int alarmInterval = 1000 * 60 * 15;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.e("hagai", "onReceive: i'm awake");
        String action = intent.getAction() != null ? intent.getAction() : WAKE_ME_UP;
        if(isMyServiceRunning(context, BLEForegroundService.class))
        {
            //do nothing
            CSVUtil.writeToSDFile(null,true," action is " + action);
        }
        else
        {
            CSVUtil.writeToSDFile(null,false," action is " + action);
            BLEForegroundService.startThisService(context);
        }

        switch (action)
        {
            case WAKE_ME_UP:
                scheduleAlarms(context,WAKE_ME_UP,alarmInterval);
                break;
            case WAKE_ME_UP_AFTER_5:
                scheduleAlarms(context,WAKE_ME_UP_AFTER_5,1000*60*5);
                break;
            case WAKE_ME_UP_AFTER_10:
                scheduleAlarms(context,WAKE_ME_UP_AFTER_10,1000*60*10);
                break;
            default:
                scheduleAlarms(context,WAKE_ME_UP,alarmInterval);
                break;
        }
    }

    public void scheduleAlarms(final Context ctx, final String action, final int delay) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
                alarmIntent.setAction(action);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
            }
        }, delay);
    }

    private boolean isMyServiceRunning(Context ctx, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
