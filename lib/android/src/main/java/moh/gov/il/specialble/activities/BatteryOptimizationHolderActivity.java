package moh.gov.il.specialble.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import moh.gov.il.specialble.util.DeviceUtil;

public class BatteryOptimizationHolderActivity extends AppCompatActivity {

    public static final String LOCAL_BROAD_CAST_INTENT = "user_interacted_with_dialog";
    private static final String TAG = "BatteryOptimizationHold";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!DeviceUtil.isBatteryOptimizationDeactivated(this)) {
            DeviceUtil.askUserToTurnDozeModeOff(this, getPackageName());
        }
        else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: requestCode - " + requestCode + " resultCode - " + resultCode );
        if(requestCode == 666) {
            Intent intent = new Intent(LOCAL_BROAD_CAST_INTENT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            finish();
        }
    }
}
