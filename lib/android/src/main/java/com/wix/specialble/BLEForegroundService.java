package com.wix.specialble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.wix.specialble.bt.BLEManager;

public class BLEForegroundService extends Service {


    public static final String CHANNEL_ID = "BLEForegroundServiceChannel";
    BLEManager bleManager;
    {
        try {
            bleManager = BLEManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler();
    private String mServiceUUID = "";
    private String mData = "";


    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            bleManager.startScan(mServiceUUID);
            bleManager.advertise(mServiceUUID, mData);
            handler.postDelayed(this, 120000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        bleManager.stopScan();
        bleManager.stopAdvertise();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceUUID = intent.getStringExtra("serviceUUID");
        mData = intent.getStringExtra("publicKey");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, BLEForegroundService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BLE Scanning")
                .setContentText("Scanning for BLE devices")
                .setSmallIcon(R.drawable.virus)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        this.handler.post(this.runnableCode);
        //stopSelf();
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
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
