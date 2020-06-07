package com.wix.specialble;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.wix.specialble.bt.BLEManager;
import com.wix.specialble.config.Config;
import com.wix.specialble.receivers.AlarmReceiver;
import com.wix.specialble.util.Constants;

import java.io.IOException;
import java.io.InputStream;

public class BLEForegroundService extends Service {
    public static final String CHANNEL_ID = "BLEForegroundServiceChannel";
    private static final String TAG = "BLEForegroundService";
    private static boolean isServiceRunning = false;

    BLEManager bleManager;
    private PowerManager.WakeLock wakeLock;


    {
        try {
            bleManager = BLEManager.getInstance(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Handler handler = new Handler();

    /**
     * Utility for starting this Service the same way from multiple places.
     */
    public static void startThisService(Context context) {
        if(!isServiceRunning && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            Intent sIntent = new Intent(context, BLEForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(sIntent);
            } else {
                context.startService(sIntent);
            }
        }
        else {
            Log.e(TAG, "startThisService: This device doesn't support BLE");
        }
    }

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            acquireWakeLock();
            bleManager.startScan();
            BLEForegroundService.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleManager.stopScan();
                    handler.postDelayed(scanRunnable,Config.getInstance(BLEForegroundService.this).getScanInterval());
                    releaseWakeLock();
                }
            }, Config.getInstance(BLEForegroundService.this).getScanDuration());
        }
    };


    private Runnable advertiseRunnable = new Runnable() {
        @Override
        public void run() {
            acquireWakeLock();
            bleManager.advertise();
            BLEForegroundService.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleManager.stopAdvertise();
                    handler.postDelayed(advertiseRunnable,Config.getInstance(BLEForegroundService.this).getAdvertiseInterval());
                    releaseWakeLock();
                }
            }, Config.getInstance(BLEForegroundService.this).getAdvertiseDuration());
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;

        if (bleManager != null) {
            bleManager.stopScan();
            bleManager.stopAdvertise();
        }
        handler.removeCallbacksAndMessages(null);

        //release wake lock
        releaseWakeLock();

        //clear any pending wake up's
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction(AlarmReceiver.WAKE_ME_UP);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        alarmIntent.setAction(AlarmReceiver.WAKE_ME_UP_AFTER_5);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        alarmIntent.setAction(AlarmReceiver.WAKE_ME_UP_AFTER_10);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!isServiceRunning) {

            isServiceRunning = true;

            createNotificationChannel();
            Config config = Config.getInstance(this);

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            PendingIntent mainActivityIntent = null;
            if (launchIntent != null) {
                mainActivityIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            Intent notificationIntent = new Intent(this, BLEForegroundService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0);
            /////////////////////////////////////////////////////////////////////////////////////////////////
            //                  Prepare icons for notification display                                     //
            //                                                                                             //
            //  The notification icon is sent from the host application via @SpecialBleModule.setConfig()  //
            //  Large icon comes from the field notificationLargeIconPath.                                 //
            //  Small icon comes from the field notificationSmallIconPath.                                 //
            //                                                                                             //
            /////////////////////////////////////////////////////////////////////////////////////////////////
            int resId = 0;
            if (config.getSmallNotificationIconPath() != null && config.getSmallNotificationIconPath().length() > 0) {
                try {
                    resId = getResources().getIdentifier(config.getSmallNotificationIconPath(), Constants.DRAWABLE, Constants.DEF_PACKAGE);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            Bitmap bitmap = null;
            if (config.getLargeNotificationIconPath() != null && config.getLargeNotificationIconPath().length() > 0) {

                try {
                    InputStream ims = getAssets().open(config.getLargeNotificationIconPath());
                    bitmap = BitmapFactory.decodeStream(ims);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(config.getNotificationTitle())
                    .setContentText(config.getNotificationContent())
                    .setSmallIcon(resId)
                    .setContentIntent(mainActivityIntent);
            if (bitmap != null) {
                notificationBuilder.setLargeIcon(bitmap);
            }
            Notification notification = notificationBuilder.build();

            startForeground(1, notification);
            // initialize if needed
            if (bleManager == null) {
                bleManager = BLEManager.getInstance(getApplicationContext());
            }
            handler.post(this.scanRunnable);
            handler.post(this.advertiseRunnable);

            //schedule wake locks every 5,10,15 minutes to make sure were awake
            //the minimum time is 15 min but the wake lock and foreground service will help in regard to this limitation
            scheduleAlarms();
        }
        return START_STICKY;
    }

    private void acquireWakeLock()
    {
        if (wakeLock == null)
        {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.WAKELOCK_TAG);
            wakeLock.acquire(Constants.TIMEOUT_WAKELOCK);
        }
    }

    private void releaseWakeLock() {
        if(wakeLock != null && wakeLock.isHeld())
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void scheduleAlarms()
    {
        AlarmReceiver.scheduleAlarms(BLEForegroundService.this, AlarmReceiver.WAKE_ME_UP, AlarmReceiver.ALARM_INTERVAL);

        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                AlarmReceiver.scheduleAlarms(BLEForegroundService.this, AlarmReceiver.WAKE_ME_UP_AFTER_5, AlarmReceiver.ALARM_INTERVAL);
            }
        },AlarmReceiver.ALARM_INTERVAL_AFTER_5);

        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                AlarmReceiver.scheduleAlarms(BLEForegroundService.this, AlarmReceiver.WAKE_ME_UP_AFTER_10, AlarmReceiver.ALARM_INTERVAL);
            }
        },AlarmReceiver.ALARM_INTERVAL_AFTER_10);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
