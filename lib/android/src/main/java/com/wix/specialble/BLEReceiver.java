package com.wix.specialble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Currently filtering in only BOOT_COMPLETE and MY_PACKEGE_REPLACED.
 * NOTE: MY_PACKAGE_REPLACED was added after PACKAGE_REPLACED and is meant to prevent waking the app up every time any app is replaced on device.
 *
 */
public class BLEReceiver extends BroadcastReceiver {
    private static final String TAG = BLEReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent arg1) {
        //TODO: Working according to GitHub issue: https://github.com/wix-incubator/rn-contact-tracing/issues/10

        //TODO: What to argue in serviceUUID & publicKey (Lev: "they should be in sPrefs coming from a ReactNative config") //see SpecialBleModule.setConfig()
        //          Asked Lev some questoins in github issue + He said Kobi is addressing this issue to some part so lets see if we can pull his work
        //TODO: We might need the intent that starts the service to have intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); if we're starting on boot
        //TODO: Make sure other app replacements aren't received here (if so, we'd need to filter using if (intent.getDataString().contains("com.aaa.bbb")){..
        //TODO: Not finding QUICKBOOT_POWERON in Intent.java, this might cause issues
//        BLEForegroundService.startThisService();

        Log.d(TAG, "Restarting BLE service..");
    }
}