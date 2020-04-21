package com.wix.specialble;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.wix.specialble.bt.BLEManager;
import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Scan;
import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.kays.PublicKey;
import com.wix.specialble.util.CSVUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpecialBleModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final BLEManager bleManager;
    private static final String TAG = "SpecialBleModule";

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
    public void advertise() {
        Config config  = Config.getInstance(reactContext);
        bleManager.advertise(config.getServiceUUID(), config.getToken());
    }

    @ReactMethod
    public void stopAdvertise() {
        bleManager.stopAdvertise();
    }

    @ReactMethod
    public void startBLEScan() {
        Config config  = Config.getInstance(reactContext);
        bleManager.startScan(config.getServiceUUID());
    }

    @ReactMethod
    public void stopBLEScan() {
        bleManager.stopScan();
    }


    @ReactMethod
    private void startBLEService() {
        Intent sIntent = new Intent(this.reactContext, BLEForegroundService.class);
        Config config  = Config.getInstance(reactContext);
        sIntent.putExtra("serviceUUID", config.getServiceUUID());
        sIntent.putExtra("publicKey",config.getToken());
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
    public void cleanScansDB() {
        DBClient.getInstance(reactContext).clearAllScans();
    }

    @ReactMethod
    public void getAllScans(Callback callback) {
        List<Scan> scans = bleManager.getAllScans();
        WritableArray retArray = new WritableNativeArray();
        for(Scan scan : scans){
            retArray.pushMap(scan.toWritableMap());
        }
        callback.invoke(retArray);
    }

    @ReactMethod
    public void getScansByKey(String pubKey, Callback callback) {
        List<Scan> scans = bleManager.getScansByKey(pubKey);
        WritableArray retArray = new WritableNativeArray();
        for(Scan scan : scans){
            retArray.pushMap(scan.toWritableMap());
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


    @ReactMethod
    public void getConfig(Callback callback) {
        Config config = Config.getInstance(reactContext);
        WritableMap configMap = new WritableNativeMap();
        configMap.putString("token", config.getToken());
        configMap.putString("serviceUUID", config.getServiceUUID());
        configMap.putDouble("scanDuration", config.getScanDuration());
        configMap.putDouble("scanInterval", config.getScanInterval());
        configMap.putInt("scanMode", config.getScanMode()); //
        configMap.putInt("scanMatchMode", config.getScanMatchMode());
        configMap.putDouble("advertiseDuration", config.getAdvertiseDuration());
        configMap.putDouble("advertiseInterval", config.getAdvertiseInterval());
        configMap.putInt("advertiseMode", config.getAdvertiseMode());
        configMap.putInt("advertiseTXPowerLevel", config.getAdvertiseTXPowerLevel());
        callback.invoke(configMap);
    }


    @ReactMethod
    public void setConfig(ReadableMap configMap) {
        Config config = Config.getInstance(reactContext);
        config.setToken(configMap.getString("token"));
        config.setServiceUUID(configMap.getString("serviceUUID"));
        config.setScanDuration((long) configMap.getDouble("scanDuration"));
        config.setScanInterval((long) configMap.getDouble("scanInterval"));
        config.setScanMatchMode(configMap.getInt("scanMatchMode"));
        config.setAdvertiseDuraton((long) configMap.getDouble("advertiseDuration"));
        config.setAdvertiseInterval((long) configMap.getDouble("advertiseInterval"));
        config.setAdvertiseMode(configMap.getInt("advertiseMode"));
        config.setAdvertiseTXPowerLevel(configMap.getInt("advertiseTXPowerLevel"));
    }

    @ReactMethod
    public void exportAllDevicesCsv() {
        try {
            CSVUtil.saveAllDevicesAsCSV(reactContext, bleManager.getAllDevices());
            shareFile(CSVUtil.getDevicesCsvFile(reactContext));
        } catch (Exception e) {
            Log.e(TAG, "exportAllDevicesCsv: "+e.getMessage(),e); //handle exception
        }
    }

    @ReactMethod
    public void exportAllScansCsv() {
        try {
            CSVUtil.saveAllScansAsCSV(reactContext, bleManager.getAllScans());
            shareFile(CSVUtil.getScansCsvFile(reactContext));
        } catch (Exception e) {
            Log.e(TAG, "exportAllScansCsv: "+e.getMessage(),e); //handle exception
        }
    }

    private void shareFile(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        Uri fileUri = FileProvider.getUriForFile(reactContext, "com.wix.specialble" + ".provider",file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(shareIntent, "");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(chooser);
    }
}
