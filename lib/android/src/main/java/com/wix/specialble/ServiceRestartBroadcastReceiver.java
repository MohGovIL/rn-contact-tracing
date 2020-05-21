package com.wix.specialble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hagai on 21/05/2020.
 */
public class ServiceRestartBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_RESTART_BLUETOOTH_SERVICE = "ACTION_RESTART_BLUETOOTH_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_RESTART_BLUETOOTH_SERVICE)) {
            BLEForegroundService.startThisService(context);
        }
    }

    public static void sendBroadcast(Context context)
    {
        Intent sIntent = new Intent(context, ServiceRestartBroadcastReceiver.class);
        sIntent.setAction(ACTION_RESTART_BLUETOOTH_SERVICE);
        context.sendBroadcast(sIntent);
    }
}
