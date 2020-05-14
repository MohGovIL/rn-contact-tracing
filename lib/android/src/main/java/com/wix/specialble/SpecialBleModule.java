package com.wix.specialble;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wix.crypto.Contact;
import com.wix.crypto.Crypto;
import com.wix.crypto.CryptoManager;
import com.wix.crypto.Match;
import com.wix.crypto.User;
import com.wix.crypto.utilities.BytesUtils;
import com.wix.crypto.utilities.Hex;
import com.wix.specialble.bt.BLEManager;
import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Scan;
import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.kays.PublicKey;
import com.wix.specialble.util.CSVUtil;
import com.wix.specialble.util.DeviceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialBleModule extends ReactContextBaseJavaModule {


    private final ReactApplicationContext reactContext;
    private final BLEManager bleManager;
    private static final String TAG = "SpecialBleModule";
    private EventToJSDispatcher mEventToJSDispatcher;


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
    private void startBLEService() {
        BLEForegroundService.startThisService(this.reactContext);
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

    private void shareFile(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        Uri fileUri = FileProvider.getUriForFile(reactContext, "com.wix.specialble" + ".provider", file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(shareIntent, "");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(chooser);
    }

    @ReactMethod
    public void requestToDisableBatteryOptimization(){
        DeviceUtil.askUserToTurnDozeModeOff(getCurrentActivity(), getReactApplicationContext().getPackageName());
    }

    @ReactMethod
    public void isBatteryOptimizationDeactivated(Promise promise) {
        promise.resolve(DeviceUtil.isBatteryOptimizationDeactivated(reactContext));
    }

    @ReactMethod
    public void deleteDatabase() {
         bleManager.clearAllDevices();
    }

    @ReactMethod
    public String fetchInfectionDataByConsent()
    {
        return infectedDbToJson(CryptoManager.getInstance(reactContext).fetchInfectionDataByConsent());
    }

    @ReactMethod
    public String match(String epochs)
    {
        //loadDatabase(reactContext.getApplicationContext());
        Map<Integer, Map<Integer, ArrayList<byte[]>>> infe = extractInfectedDbFromJson(epochs); //TODO::pass epochs when ready
        List<Match> result = CryptoManager.getInstance(reactContext).mySelf.findCryptoMatches(infe);
        if(result.size() > 0)
        {
            Toast.makeText(reactContext.getApplicationContext(),"We Found a Match!! :(",Toast.LENGTH_LONG).show();
        }
        return parseResultToJson(result);
    }

    private String infectedDbToJson(Map<Integer, Map<Integer, ArrayList<byte[]>>> infectedDb)
    {
        JSONObject root = new JSONObject();
        JSONArray rootInfected = new JSONArray();

        try
        {
            boolean first = true;
            Object[] keySetArray = infectedDb.keySet().toArray();
            for (int k = 0; k < 14 ; k++)
            {
                int day = -1;
                if(k < keySetArray.length)
                {
                    day = (int) keySetArray[k];
                }
                if (first)
                {
                    root.put("startDay",day);
                    first = false;
                }

                Map<Integer, ArrayList<byte[]>> epochs = infectedDb.get(day);
                JSONArray rootInfectedEpochs = new JSONArray();
                if(epochs != null)
                {
                    Object[] epochKeySetArray = epochs.keySet().toArray();
                    for (int x = 0; x < 24; x++)
                    {
                        int epocKey = -1;
                        if (x < epochKeySetArray.length)
                        {
                            epocKey = (int) epochKeySetArray[x];
                        }
                        ArrayList<byte[]> ephs = epochs.get(epocKey);
                        JSONArray rootInfectedEpochsInnerLevel = new JSONArray();

                        if (ephs != null)
                        {
                            for (int i = 0; i < ephs.size(); i++)
                            {
                                String converted = Hex.toHexString(ephs.get(i), null);
                                rootInfectedEpochsInnerLevel.put(converted);
                            }
                        }
                        rootInfectedEpochs.put(rootInfectedEpochsInnerLevel);
                    }
                }
                rootInfected.put(rootInfectedEpochs);
            }
            root.put("infected",rootInfected);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root.toString();
    }

    private Map<Integer, Map<Integer, ArrayList<byte[]>>> extractInfectedDbFromJson(String epochs)
    {
        Map<Integer, Map<Integer, ArrayList<byte[]>>> infectedDb = new HashMap<>();
        try
        {
            JSONObject jsonRes;
            if(epochs != null)
                jsonRes = new JSONObject(epochs);
            else
                jsonRes = new JSONObject(loadJSONFromAsset(reactContext.getApplicationContext())); ///for testing

            JSONArray infected = jsonRes.getJSONArray("infected");
            int startDay = jsonRes.getInt("startDay");

            for (int i = 0; i < infected.length(); i++,startDay++)
            {
                infectedDb.put(startDay, new HashMap<Integer, ArrayList< byte[]>>());
                JSONArray epochsArray = infected.getJSONArray(i);

                for (int j = 0; j < epochsArray.length(); j++)
                {
                    JSONArray eph = epochsArray.getJSONArray(j);
                    infectedDb.get(startDay).put(j, new ArrayList<byte[]>());
                    for (int k = 0; k < eph.length(); k++) {
                        String epoc = eph.getString(k);
                        byte[] epocBytes = Hex.hexStringToByteArray(epoc);
                        infectedDb.get(startDay).get(j).add(epocBytes);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return infectedDb;
    }

    private String loadJSONFromAsset(Context ctx) {
        String json = null;
        try {
            InputStream is = ctx.getResources().openRawResource(R.raw.infected);//ctx.getAssets().open("infected.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void loadDatabase(Context ctx)
    {
        String json = null;
        try {
            InputStream is = ctx.getResources().openRawResource(R.raw.outputcontacts);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONArray dbArray = new JSONArray(json);

            for (int i = 0; i < dbArray.length(); i++)
            {
                JSONObject jo = dbArray.getJSONObject(i);

                byte[] otherEphemeralId = Hex.hexStringToByteArray(jo.getString("ephemeral_id"));
                byte[] rssi = BytesUtils.numToBytes(jo.getInt("rssi"),4);
                byte[] ownLocation = Hex.hexStringToByteArray(jo.getString("geohash"));
                int time = jo.getInt("timestamp");
                DBClient.getInstance(reactContext).storeContact(new Contact(otherEphemeralId, rssi, time, ownLocation));
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String parseResultToJson(List<Match> result)
    {
        return "";
    }

}
