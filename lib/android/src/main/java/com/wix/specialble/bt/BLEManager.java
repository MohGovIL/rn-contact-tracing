package com.wix.specialble.bt;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.listeners.IEventListener;

import java.util.List;


public class BLEManager implements IEventListener {

    private static final String TAG = "BLEManager";

    private BluetoothAdapter bluetoothAdapter;
    Context context;
    private BLEScannerManager bleScanner;
    private BLEAdvertisingManager bleAdvertiser;
    private static BLEManager sBLEManagerInstance;
    private Config mConfig;
    private MutableLiveData<Pair<String, Object>> mEventLiveData =  new MutableLiveData<>();

    public enum BLEProtocol {
        GAP, GATT
    }

    private BLEManager(Context context) {
        this.context = context;
        init();
    }

    public static BLEManager getInstance(Context context) {
        if (sBLEManagerInstance == null) {
            sBLEManagerInstance = new BLEManager(context);
        }
        return sBLEManagerInstance;
    }

    public static BLEManager getInstance() throws Exception {
        if (sBLEManagerInstance == null) {
            throw new Exception("missing context");
        }
        return sBLEManagerInstance;
    }

    public void init() {
        mConfig = Config.getInstance(context);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
            bleScanner = new BLEScannerManager(context, this);
            bleAdvertiser = new BLEAdvertisingManager(context, this);
        }
    }

    public void startScan() {
        bleScanner.startScan(mConfig.getServiceUUID());
    }

    public void stopScan() {
        bleScanner.stopScan();
    }

    public void advertise() {
        bleAdvertiser.startAdvertise(mConfig.getServiceUUID(), mConfig.getToken());
    }

    public void stopAdvertise() {
        bleAdvertiser.stopAdvertise();
    }


    public List<Device> getAllDevices() {
        return DBClient.getInstance(context).getAllDevices();
    }

    public void clearAllDevices() {
        DBClient.getInstance(context).clearAllDevices();
    }

    public List<Scan> getAllScans() {
        return DBClient.getInstance(context).getAllScans();
    }

    public List<Scan> getScansByKey(String pubKey) {
        return DBClient.getInstance(context).getScansByKey(pubKey);
    }

    public MutableLiveData<Pair<String, Object>> getEventLiveData(){
        return mEventLiveData;
    }

    @Override
    public void onEvent(String event, Object data) {
        mEventLiveData.postValue(new Pair(event, data));
    }
}