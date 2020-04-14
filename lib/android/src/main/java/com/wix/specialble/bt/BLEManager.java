package com.wix.specialble.bt;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.wix.specialble.db.DBClient;

import java.util.List;


public class BLEManager {

    private static final String TAG = "BLEManager";

    private BluetoothAdapter bluetoothAdapter;
    ReactApplicationContext context;
    private BLEScannerManager bleScanner;
    private BLEAdvertisingManager bleAdvertiser;

    private static BLEManager sBLEManagerInstance;

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
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
            bleScanner = BLEScannerManager.getInstance(context, bluetoothAdapter);
            bleAdvertiser = BLEAdvertisingManager.getInstance(context, bluetoothAdapter);
        }
    }

    public void startScan(String serviceUUID) {
        bleScanner.startScan(serviceUUID);
    }

    public void stopScan() {
        bleScanner.stopScan();
    }

    public void advertise(String serviceUUID, String publicKey) {
        bleAdvertiser.startAdvertise(serviceUUID, publicKey);
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
}
