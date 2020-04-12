package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.ReadableType;
import com.wix.specialble.EventToJSDispatcher;
import com.wix.specialble.db.DBClient;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEScannerManager {

    private static BLEScannerManager sScannerManager;

    ReactApplicationContext context;
    BluetoothAdapter bluetoothAdapter;
    SpecialBLEScanCallback bleScanCallback;
    DBClient dbClient;

    private String TAG = "BLEScannerManager";

    private BLEScannerManager(ReactApplicationContext context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
        bleScanCallback = new SpecialBLEScanCallback();
        dbClient = DBClient.getInstance(context);
    }

    public static BLEScannerManager getInstance(ReactApplicationContext context, BluetoothAdapter bluetoothAdapter) {
        if (sScannerManager != null) {
            return sScannerManager;
        }
        return new BLEScannerManager(context, bluetoothAdapter);
    }


    public void startScan(String serviceUUID) {
        bluetoothAdapter.enable();
        if (bluetoothAdapter.isEnabled()) {
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID)))
                    .build();
            ArrayList filters = new ArrayList<ScanFilter>();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder().build();

            bluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(filters, settings, bleScanCallback);
            EventToJSDispatcher.getInstance(context).sendScanningStatus(true);
        }
    }

    public void stopScan() {
        bluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(bleScanCallback);
        EventToJSDispatcher.getInstance(context).sendScanningStatus(false);
    }


    class SpecialBLEScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            byte [] bytePK = result.getScanRecord().getManufacturerSpecificData(1023);
            String pk = bytePK!=null ? new String (bytePK,Charset.forName("UTF-8")) : "NaN";
            super.onScanResult(callbackType, result);
            Device newDevice = new Device(pk,
                    result.getDevice().getName(),
                    result.getDevice().getAddress(),
                    "scanRecordData",
                    result.getRssi());
            updateNewDevice(newDevice);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode != SCAN_FAILED_ALREADY_STARTED) {
                EventToJSDispatcher.getInstance(context).sendScanningStatus(false);
            }
            Log.d(TAG, "onScanStartFailed - ErrorCode: " + errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }
    }

    private void updateNewDevice(final Device newDevice) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Device oldDevice = dbClient.getDeviceByKey(newDevice.getPublicKey());
                if(oldDevice==null){
                    dbClient.addDevice(newDevice);
                    EventToJSDispatcher.getInstance(context).sendNewDevice(newDevice);
                }
                else if (oldDevice.getRssi()>newDevice.getRssi()){
                    dbClient.updateDevice(newDevice);
                    EventToJSDispatcher.getInstance(context).sendNewDevice(newDevice);
                }
            }
        });
    }
}
