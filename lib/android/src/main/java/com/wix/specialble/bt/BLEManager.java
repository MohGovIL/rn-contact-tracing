package com.wix.specialble.bt;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;

import java.util.List;


public class BLEManager {

    private static final String TAG = "BLEManager";

    private BluetoothAdapter bluetoothAdapter;
    ReactApplicationContext context;
    private BLEScannerManager bleScanner;
    private BLEAdvertisingManager bleAdvertiser;
    private static BLEManager sBLEManagerInstance;
    private String mServiceUUID;
    private String mPublicKey;

    public enum BLEProtocol {
        GAP, GATT
    }

    private BLEManager(ReactApplicationContext context) {
        this.context = context;
        init();
    }

    public static BLEManager getInstance(ReactApplicationContext context) {
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
        Log.d("ahmed", "BLEManager Init was called..");
        Config config = Config.getInstance(context); //config.getServiceUUID(), config.getPublicKey()
        mServiceUUID = config.getServiceUUID();
        mPublicKey = config.getToken();

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
            bleScanner = BLEScannerManager.getInstance(context, bluetoothAdapter);
            bleAdvertiser = BLEAdvertisingManager.getInstance(context, bluetoothAdapter);
        }

    }

    public void startScan() {
        bleScanner.startScan(mServiceUUID); //TODO: ahmed, I'm worried, that now that we're getting this UUID from config in INIT, boot_complete restarts might not go through this init flow
    }

    public void stopScan() {
        bleScanner.stopScan();
    }

    public void advertise() {
        bleAdvertiser.startAdvertise(mServiceUUID, mPublicKey);
    }

    public void stopAdvertise() {
        bleAdvertiser.stopAdvertise();
    }


    public List<Device> getAllDevices(){
        return DBClient.getInstance(context).getAllDevices();
    }

    public void clearAllDevices(){
        DBClient.getInstance(context).clearAllDevices();
    }

    public List<Scan> getAllScans(){ return DBClient.getInstance(context).getAllScans();
    }

    public List<Scan> getScansByKey(String pubKey){ return DBClient.getInstance(context).getScansByKey(pubKey);
    }
}
