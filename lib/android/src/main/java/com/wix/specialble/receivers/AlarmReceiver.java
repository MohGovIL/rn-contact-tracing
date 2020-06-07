package com.wix.specialble.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import com.wix.specialble.BLEForegroundService;

/**
 * Created by hagai on 30/04/2020.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String WAKE_ME_UP = "wake.me.up";
    public static final String WAKE_ME_UP_AFTER_5 = "wake.me.up.after.5";
    public static final String WAKE_ME_UP_AFTER_10 = "wake.me.up.after.10";
    public static int ALARM_INTERVAL = 1000 * 60 * 15;
    public static int ALARM_INTERVAL_AFTER_5 = 1000 * 60 * 5;
    public static int ALARM_INTERVAL_AFTER_10 = 1000 * 60 * 10;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction() != null ? intent.getAction() : WAKE_ME_UP;
        BLEForegroundService.startThisService(context);
        Log.e("AlarmReceiver", "Alarm manager onReceive action: " +  intent.getAction());
        switch (action)
        {
            case WAKE_ME_UP:
                scheduleAlarms(context,WAKE_ME_UP, ALARM_INTERVAL);
                break;
            case WAKE_ME_UP_AFTER_5:
                scheduleAlarms(context,WAKE_ME_UP_AFTER_5, ALARM_INTERVAL);
                break;
            case WAKE_ME_UP_AFTER_10:
                scheduleAlarms(context,WAKE_ME_UP_AFTER_10, ALARM_INTERVAL);
                break;
        }
    }

    public static void scheduleAlarms(final Context ctx, final String action, final int delay)
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
}
