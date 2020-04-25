package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.listeners.IEventListener;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEScannerManager {

    public static final String SCANNING_STATUS = "scanningStatus";
    public static final String FOUND_DEVICE = "foundDevice";
    public static final String FOUND_SCAN = "foundScan";
    private static BLEScannerManager sScannerManager;

    Context mContext;
    BluetoothAdapter bluetoothAdapter;
    SpecialBLEScanCallback bleScanCallback;
    DBClient dbClient;

    private String TAG = "BLEScannerManager";
    private IEventListener mEventListenerCallback;

    BLEScannerManager(Context context, IEventListener eventListenerCallback) {
        mContext = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleScanCallback = new SpecialBLEScanCallback();
        dbClient = DBClient.getInstance(context);
        mEventListenerCallback = eventListenerCallback;
    }

    public static BLEScannerManager getInstance(Context context, IEventListener eventListenerCallback) {
        if (sScannerManager != null) {
            return sScannerManager;
        }
        return new BLEScannerManager(context, eventListenerCallback);
    }


    public void startScan(String serviceUUID) {
        bluetoothAdapter.enable();
        if (bluetoothAdapter.isEnabled()) {

            Config config = Config.getInstance(mContext);
            int scanMatchMode = config.getScanMatchMode();

            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID))).build();
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);

            ScanSettings settings = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                settings = new ScanSettings.Builder().setMatchMode(scanMatchMode).build();
            }
            else{
                settings = new ScanSettings.Builder().build();
            }

            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, bleScanCallback);
            mEventListenerCallback.onEvent(SCANNING_STATUS, true);
        }
    }

    public void stopScan() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(bleScanCallback);
        mEventListenerCallback.onEvent(SCANNING_STATUS, false);
    }

    class SpecialBLEScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) {
                Log.e(TAG, "onScanResult: scanRecord is null!");
                return;
            }

            if (scanRecord.getServiceUuids() == null) {
                Log.e(TAG, "onScanResult: getServiceUuids is null!");
                return;
            }

            ParcelUuid pUuid = scanRecord.getServiceUuids().get(0);
            byte[] byteScannedToken = result.getScanRecord().getServiceData(pUuid);

            String ScannedToken = byteScannedToken != null ? new String(byteScannedToken, Charset.forName("UTF-8")) : "NaN";

            int tx = scanRecord.getTxPowerLevel();

            super.onScanResult(callbackType, result);
            handleScanResults(result, ScannedToken, tx);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode != SCAN_FAILED_ALREADY_STARTED) {
                mEventListenerCallback.onEvent(SCANNING_STATUS,false);
            }
            Log.d(TAG, "onScanStartFailed - ErrorCode: " + errorCode);
        }
    }

    private void handleScanResults(final ScanResult result, final String scannedToken, final int tx) {
        // handle devices
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Device oldDevice = dbClient.getDeviceByKey(scannedToken); // get device from database
                Device newDevice;

                if (oldDevice != null) {
                    newDevice = getNewDevice(result, tx, scannedToken, oldDevice.getFirstTimestamp(), System.currentTimeMillis());
                    if (hasUpdateRequirements(oldDevice, result)) {
                        dbClient.updateDevice(newDevice);
                    }
                } else {
                    newDevice = getNewDevice(result, tx, scannedToken, System.currentTimeMillis(), System.currentTimeMillis());
                    dbClient.addDevice(newDevice);
                }
                mEventListenerCallback.onEvent(FOUND_DEVICE, newDevice.toWritableMap());

                // handle scans
                Scan newScan = new Scan(System.currentTimeMillis(),
                        scannedToken,
                        result.getDevice().getAddress(),
                        BLEManager.BLEProtocol.GAP.toString(),
                        result.getRssi(),
                        tx);
                dbClient.addScan(newScan);
                mEventListenerCallback.onEvent(FOUND_SCAN, newScan.toWritableMap());
            }
        });
    }

    private boolean hasUpdateRequirements(Device oldDevice, ScanResult result) {
        return (oldDevice.getRssi()>result.getRssi()+3 || oldDevice.getRssi()<result.getRssi()-3);
    }

    private Device getNewDevice(ScanResult result, int tx, String scannedToken, long firstSeenTime, long lastSeenTime) {
        return new Device(firstSeenTime, lastSeenTime, scannedToken, result.getDevice().getAddress(),
                BLEManager.BLEProtocol.GAP.toString(), result.getRssi(), tx);
    }
}