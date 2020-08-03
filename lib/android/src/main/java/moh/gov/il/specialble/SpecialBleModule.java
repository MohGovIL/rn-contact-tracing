package moh.gov.il.specialble;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import moh.gov.il.specialble.bt.BLEManager;
import moh.gov.il.specialble.bt.Device;
import moh.gov.il.specialble.bt.Scan;
import moh.gov.il.specialble.db.DBClient;
import moh.gov.il.specialble.kays.PublicKey;
import moh.gov.il.specialble.util.PrefUtils;
import moh.gov.il.crypto.CryptoManager;
import moh.gov.il.crypto.MatchResponse;
import moh.gov.il.specialble.activities.BatteryOptimizationHolderActivity;
import moh.gov.il.specialble.config.Config;
import moh.gov.il.specialble.util.CSVUtil;
import moh.gov.il.specialble.util.DeviceUtil;
import moh.gov.il.specialble.util.ParseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpecialBleModule extends ReactContextBaseJavaModule {


    private final ReactApplicationContext reactContext;
    private final BLEManager bleManager;
    private static final String TAG = "SpecialBleModule";
    private EventToJSDispatcher mEventToJSDispatcher;
    private Callback mBatteryOptimizationCallback;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SpecialBleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        mEventToJSDispatcher = EventToJSDispatcher.getInstance(reactContext);
        // init crypto lib //
        /////////////////////
        CryptoManager.getInstance(reactContext);

        bleManager = BLEManager.getInstance(reactContext);
        bleManager.setEventToJSDispatcher(mEventToJSDispatcher);
        LocalBroadcastManager.getInstance(reactContext).registerReceiver(batteryOptimizationReceiver,
                new IntentFilter(BatteryOptimizationHolderActivity.LOCAL_BROAD_CAST_INTENT ));

//        ParseUtils.loadDatabase(reactContext.getApplicationContext());//open this to load db for testing from raw...

        //  registerEventLiveData();

    }

/*    private void registerEventLiveData() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                bleManager.getEventLiveData().observeForever(new Observer<Pair<String, Object>>() {
                    @Override
                    public void onChanged(Pair<String, Object> event) {
                        mEventToJSDispatcher.onEvent(event.first, event.second);
                    }
                });
            }
        });
    }*/


    @Override
    public String getName() {
        return "SpecialBle";
    }


    @ReactMethod
    public void advertise() {
        bleManager.advertise();
    }

    @ReactMethod
    public void stopAdvertise() {
        bleManager.stopAdvertise();
    }

    @ReactMethod
    public void startBLEScan() {
        bleManager.startScan();
    }

    @ReactMethod
    public void stopBLEScan() {
        bleManager.stopScan();
    }

    @ReactMethod
    public void askToDisableBatteryOptimization() {
        if(!DeviceUtil.isBatteryOptimizationDeactivated(reactContext)) {
            DeviceUtil.askUserToTurnDozeModeOff(getCurrentActivity(), getReactApplicationContext().getPackageName());
        }
    }

    @ReactMethod
    private void startBLEService() {

        PrefUtils.setStartServiceValue(this.reactContext, true);
        if(!DeviceUtil.isBatteryOptimizationDeactivated(reactContext) && Config.getInstance(reactContext).getDisableBatteryOptimization()) {
            DeviceUtil.askUserToTurnDozeModeOff(getCurrentActivity(), getReactApplicationContext().getPackageName());
        }
        BLEForegroundService.startThisService(this.reactContext);
    }

    @ReactMethod
    public void stopBLEService() {

        if(BLEForegroundService.isServiceRunning()) {
            PrefUtils.setStartServiceValue(this.reactContext, false);
            BLEForegroundService.setServiceRunningValue(false);
            this.reactContext.stopService(new Intent(this.reactContext, BLEForegroundService.class));
        }
    }

    @ReactMethod
    public void cleanDevicesDB() {
        DBClient.getInstance(reactContext).clearAllDevices();
    }

    @ReactMethod
    public void getAllDevices(Callback callback) {
        List<Device> devices = bleManager.getAllDevices();
        WritableArray retArray = new WritableNativeArray();
        for (Device device : devices) {
            retArray.pushMap(device.toWritableMap());
        }
        callback.invoke(null, retArray); // need to remove this null, it only because ios added it for some reason
    }

    @ReactMethod
    public void cleanScansDB() {
        DBClient.getInstance(reactContext).clearAllScans();
    }

    @ReactMethod
    public void getAllScans(Callback callback) {
        List<Scan> scans = bleManager.getAllScans();
        WritableArray retArray = new WritableNativeArray();
        for (Scan scan : scans) {
            retArray.pushMap(scan.toWritableMap());
        }
        callback.invoke(retArray);
    }

    @ReactMethod
    public void getScansByKey(String pubKey, Callback callback) {
        List<Scan> scans = bleManager.getScansByKey(pubKey);
        WritableArray retArray = new WritableNativeArray();
        for (Scan scan : scans) {
            retArray.pushMap(scan.toWritableMap());
        }
        callback.invoke(retArray);
    }


    @ReactMethod
    public void setPublicKeys(ReadableArray pubKeys) {
        ArrayList<PublicKey> pkList = new ArrayList<>();
        for (int i = 0; i < pubKeys.size(); i++) {
            String pkString = pubKeys.getString(i);
            PublicKey pk = new PublicKey(i, pkString);
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
        configMap.putString("notificationTitle", config.getNotificationTitle());
        configMap.putString("notificationContent", config.getNotificationContent());
        configMap.putString("notificationLargeIconPath", config.getLargeNotificationIconPath());
        configMap.putString("notificationSmallIconPath", config.getSmallNotificationIconPath());
        configMap.putBoolean("disableBatteryOptimization", config.getDisableBatteryOptimization());
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
        config.setNotificationTitle(configMap.getString("notificationTitle"));
        config.setNotificationContent(configMap.getString("notificationContent"));
        config.setLargeNotificationIconPath(configMap.getString("notificationLargeIconPath"));
        config.setSmallNotificationIconPath(configMap.getString("notificationSmallIconPath"));
        config.setDisableBatteryOptimization(configMap.getBoolean("disableBatteryOptimization"));
    }

    @ReactMethod
    public void exportAdvertiseAsCSV() {

        try {
            CSVUtil.saveAllAdvertiseAsCSV(reactContext, bleManager.getAllAdvertiseData());
            shareFile(CSVUtil.getAdvertiseCsvFile(reactContext));
        }
        catch (Exception e) {
            Log.e(TAG, "exportAdvertiseAsCSV" + e.getMessage(), e );
        }

    }

    @ReactMethod
    public void exportScansDataAsCSV() {
        try {
            CSVUtil.saveAllScansDataAsCSV(reactContext, bleManager.getAllScansData());
            shareFile(CSVUtil.getScansDataCsvFile(reactContext));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @ReactMethod
    public void exportAllDevicesCsv() {
        try {
            CSVUtil.saveAllDevicesAsCSV(reactContext, bleManager.getAllDevices());
            shareFile(CSVUtil.getDevicesCsvFile(reactContext));
        } catch (Exception e) {
            Log.e(TAG, "exportAllDevicesCsv: " + e.getMessage(), e); //handle exception
        }
    }

    @ReactMethod
    public void exportAllScansCsv() {
        try {
            CSVUtil.saveAllScansAsCSV(reactContext, bleManager.getAllScans());
            shareFile(CSVUtil.getScansCsvFile(reactContext));
        } catch (Exception e) {
            Log.e(TAG, "exportAllScansCsv: " + e.getMessage(), e); //handle exception
        }
    }

    @ReactMethod
    public void exportScansByKeyAsCSV(String key) {
        try {
            CSVUtil.saveScansByKeyAsCsv(reactContext, bleManager.getScansByKey(key), key);
            shareFile(CSVUtil.getScanByKeyCsvFile(reactContext, key));
        } catch (Exception e) {
            Log.e(TAG, "exportScansByKeyCsv: " + e.getMessage(), e); //handle exception
        }
    }

    @ReactMethod
    public void exportAllContactsAsCsv() {
        try {
            CSVUtil.saveAllContactsAsCSV(reactContext, bleManager.getAllContacts());
            shareFile(CSVUtil.getContactsCsvFile(reactContext));
        }
        catch (Exception e) {
            Log.e(TAG, "exportAllContactsAsCsv: " + e.getMessage(), e );
        }
    }

    private void shareFile(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        Uri fileUri = FileProvider.getUriForFile(reactContext, reactContext.getPackageName() + ".provider", file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(shareIntent, "");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(chooser);
    }

    @ReactMethod
    public void requestToDisableBatteryOptimization(Callback callback){

        if(!DeviceUtil.isBatteryOptimizationDeactivated(reactContext)) {
            Intent in = new Intent(getCurrentActivity(), BatteryOptimizationHolderActivity.class);
            getCurrentActivity().startActivity(in);
            mBatteryOptimizationCallback = callback;
        }
    }

    @ReactMethod
    public void isBatteryOptimizationDeactivated(Promise promise) {
        promise.resolve(DeviceUtil.isBatteryOptimizationDeactivated(reactContext));
    }

    @ReactMethod
    public void deleteDatabase() {
        bleManager.wipeDatabase();
    }

    @ReactMethod
    public void fetchInfectionDataByConsent(Callback callback)
    {
        Map<Integer, Map<Integer, ArrayList<byte[]>>> results = CryptoManager.getInstance(reactContext).fetchInfectionDataByConsent();
        callback.invoke(ParseUtils.infectedDbToJson(results));
    }

    @ReactMethod
    public void match(String epochs, Callback callback)
    {
        Map<Integer, Map<Integer, ArrayList<byte[]>>> infe = ParseUtils.extractInfectedDbFromJson(epochs, reactContext.getApplicationContext()); //TODO::pass epochs when ready
        List<MatchResponse> result = CryptoManager.getInstance(reactContext).mySelf.findCryptoMatches(infe);
        if(result != null && result.size() > 0)
        {
            Log.e(TAG, "match: We Found a Match!!");
        }
        callback.invoke(ParseUtils.parseResultToJson(result));
    }

    @ReactMethod
    public void writeContactsToDB(String db)
    {
        ParseUtils.loadDatabase(reactContext.getApplicationContext(), db);
    }

    BroadcastReceiver batteryOptimizationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DeviceUtil.isBatteryOptimizationDeactivated(reactContext))
                mBatteryOptimizationCallback.invoke(true);
            else
                mBatteryOptimizationCallback.invoke(false);
        }
    };
}
