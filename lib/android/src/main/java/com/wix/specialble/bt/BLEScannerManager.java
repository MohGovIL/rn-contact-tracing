package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.wix.specialble.EventToJSDispatcher;
import com.wix.specialble.config.Config;
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

            Config config = Config.getInstance(context);
            serviceUUID = TextUtils.isEmpty(serviceUUID) ? config.getServiceUUID() : serviceUUID;
            int scanMode = config.getScanMode();
            int scanMatchMode = config.getScanMatchMode();
            long scanDuration = config.getScanDuration();


            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID))).build();
            ArrayList filters = new ArrayList<ScanFilter>();
            filters.add(filter);

            ScanSettings settings = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                settings = new ScanSettings.Builder().setMatchMode(scanMatchMode).build();
            }
            else{
                settings = new ScanSettings.Builder().build();
            }

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

            ParcelUuid pUuid = result.getScanRecord().getServiceUuids().get(0);
            byte [] bytePK = result.getScanRecord().getServiceData(pUuid);

            String pk = bytePK!=null ? new String (bytePK,Charset.forName("UTF-8")) : "NaN";

            int tx = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                tx= result.getTxPower();
            }
            super.onScanResult(callbackType, result);
            handleScanResults(result, pk, tx);
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

    private void handleScanResults(final ScanResult result, final String pk, final int tx) {
        // handle devices
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Device oldDevice = dbClient.getDeviceByKey(pk); // get device from database
                Device newDevice;

                if (oldDevice != null) {
                    newDevice = getNewDevice(result, tx, pk, oldDevice.getFirstTimestamp(), System.currentTimeMillis());
                    if (hasUpdateRequirements(oldDevice, result)) {
                        dbClient.updateDevice(newDevice);
                    }
                } else {
                    newDevice = getNewDevice(result, tx, pk, System.currentTimeMillis(), System.currentTimeMillis());
                    dbClient.addDevice(newDevice);
                }

                EventToJSDispatcher.getInstance(context).sendNewDevice(newDevice);

                // handle scans
                Scan newScan = new Scan(System.currentTimeMillis(),
                        pk,
                        result.getDevice().getAddress(),
                        BLEManager.BLEProtocol.GAP.toString(),
                        result.getRssi(),
                        tx);
                dbClient.addScan(newScan);
                EventToJSDispatcher.getInstance(context).sendNewScan(newScan);
            }
        });
    }

    private boolean hasUpdateRequirements(Device oldDevice, ScanResult result) {
        return (oldDevice.getRssi()>result.getRssi()+3 || oldDevice.getRssi()<result.getRssi()-3);
    }

    private Device getNewDevice(ScanResult result, int tx, String publicKey, long firstSeenTime, long lastSeenTime) {
        return new Device(firstSeenTime, lastSeenTime, publicKey, result.getDevice().getAddress(),
                BLEManager.BLEProtocol.GAP.toString(), result.getRssi(), tx);
    }
}
