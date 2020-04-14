package com.wix.specialble;


import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.wix.specialble.bt.BLEManager;
import com.wix.specialble.bt.Device;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.kays.PublicKey;

import java.util.ArrayList;
import java.util.List;

public class SpecialBleModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final BLEManager bleManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SpecialBleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        bleManager = BLEManager.getInstance(reactContext);
    }

    @Override
    public String getName() {
        return "SpecialBle";
    }


    @ReactMethod
    public void advertise(String serviceUUID, String publicKey) {
        bleManager.advertise(serviceUUID, publicKey);
    }

    @ReactMethod
    public void stopAdvertise() {
        bleManager.stopAdvertise();
    }

    @ReactMethod
    public void startBLEScan(String serviceUUID) {
        bleManager.startScan(serviceUUID);
    }

    @ReactMethod
    public void stopBLEScan() {
        bleManager.stopScan();
    }


    @ReactMethod
    private void startBLEService(String serviceUUID, String publicKey) {
        Intent sIntent = new Intent(this.reactContext, BLEForegroundService.class);
        sIntent.putExtra("serviceUUID", serviceUUID);
        sIntent.putExtra("publicKey", publicKey);
        this.reactContext.startService(sIntent);
    }

    @ReactMethod
    public void stopBLEService() {
        this.reactContext.stopService(new Intent(this.reactContext, BLEForegroundService.class));
    }

    @ReactMethod
    public void cleanDevicesDB() {
        DBClient.getInstance(reactContext).clearAllDevices();
    }

    @ReactMethod
    public void getAllDevices(Callback callback) {
        List<Device> devices = bleManager.getAllDevices();
        WritableArray retArray = new WritableNativeArray();
        for(Device device : devices){
            retArray.pushMap(device.toWritableMap());
        }
        callback.invoke(retArray);
    }

    @ReactMethod
    public void setPublicKeys(ReadableArray pubKeys) {
        ArrayList<PublicKey> pkList = new ArrayList<>();
        for(int i=0; i<pubKeys.size(); i++){
            String pkString = pubKeys.getString(i);
            PublicKey pk = new PublicKey(i,pkString);
            pkList.add(pk);
        }
        DBClient.getInstance(reactContext).insertAllKeys(pkList);
    }
}
