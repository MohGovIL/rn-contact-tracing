package moh.gov.il.specialble.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import moh.gov.il.specialble.R;

public class DeviceUtil {
    private static final String TAG = DeviceUtil.class.getSimpleName();
    public static final int IGNORE_BATTERY_OPTIMIZATIONS_REQUEST_CODE = 666;


    /**
     * Same as {@link #askUserToTurnDozeModeOff(Activity, String, String, String)} but using default title and message
     *
     * @param activity    hosting activity
     * @param packageName app package name
     */
    public static void askUserToTurnDozeModeOff(final Activity activity, String packageName) {
        askUserToTurnDozeModeOff(activity, packageName, activity.getString(R.string.battery_optimization_dialog_title), activity.getString(R.string.battery_optimization_dialog_message));
    }

    /**
     * @param activity    - Not sure if Hamagen or Calibration app maybe allow both.
     * @param packageName - the app we want to handle battery optimization for.
     *                    <p>
     *                    Note: There is a more optimized variation to let the user directly whitelist the app, but it requires a permission:
     *                    Permission: <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
     *                    Source: "An app holding the REQUEST_IGNORE_BATTERY_OPTIMIZATIONS permission can trigger a system dialog to let the user add the app to the whitelist directly, without going to settings.
     *                    The app fires a ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS Intent to trigger the dialog."
     *                    <p>
     *                    Note: Be aware that people report being suspended from Google Play for using this API,
     *                    Source: https://stackoverflow.com/questions/33114063/how-do-i-properly-fire-action-request-ignore-battery-optimizations-intent
     */
    public static void askUserToTurnDozeModeOff(final Activity activity, String packageName, String title, String message) {
        if (isBatteryOptimizationDeactivated(activity)) {
            Log.d(TAG, "application already not optimized for battery saving");
            return; // no need to ask the user since battery optimization already deactivated or irrelevant ( < API 23)
        }

        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
            executeIntent(activity, new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + packageName)));  //Requires permission. Causes ActivityNotFoundException if correct package wasn't set in Intent data.
            Log.d(TAG, "Permission detected, using more optimal dialog to ask user to whitelist app.");
        } else {
            title = TextUtils.isEmpty(title) ? activity.getString(R.string.battery_optimization_dialog_title) : title;
            message = TextUtils.isEmpty(message) ? activity.getString(R.string.battery_optimization_dialog_message) : message;
            //This variation doesn't display an explanation dialog, we need to add one manually.
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Doesn't require permission. Make sure to select "All Apps" from menu -> look for "Hamagen"/Calibration App -> Select "Don't Optimize"
                            executeIntent(activity, new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            Log.d(TAG, "Permission not declared or not granted, using less optimal flow to ask user to whitelist app.");
        }
    }

    public static boolean isBatteryOptimizationDeactivated(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; // battery optimization is not supported below API 23, so we consider it as deactivated = true
    }

    private static void executeIntent(Activity activity, Intent callWhitelistIntent) {
        try {
            if (callWhitelistIntent != null) {
                activity.startActivityForResult(callWhitelistIntent, 666); //TODO: I'm thinking we don't act on result here. If user didn't whitelist us we'd just turn off in BG
            }
        } catch (ActivityNotFoundException anfe) {
            Log.d(TAG, "Activity was not found (Did you set the correct packageName in intent.setData()? | exception: " + anfe.getLocalizedMessage());
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "Exception (Most likely null permission argued in checkSelfPermission() | exception: " + iae.getLocalizedMessage());
        }
    }
}
