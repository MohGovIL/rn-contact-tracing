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

    BLEScannerManager(Context context, BluetoothAdapter bluetoothAdapter, IEventListener eventListenerCallback) {
        mContext = context;
        this.bluetoothAdapter = bluetoothAdapter; //this is the default bluetooth adapter
        bleScanCallback = new SpecialBLEScanCallback();
        dbClient = DBClient.getInstance(context);
        mEventListenerCallback = eventListenerCallback;
    }

    public static BLEScannerManager getInstance(Context context, BluetoothAdapter bluetoothAdapter, IEventListener eventListenerCallback) {
        if (sScannerManager != null) {
            return sScannerManager;
        }
        return new BLEScannerManager(context, bluetoothAdapter, eventListenerCallback);
    }


    public void startScan(String serviceUUID) {
        bluetoothAdapter.enable();
        if (bluetoothAdapter.isEnabled()) {

            Config config = Config.getInstance(mContext);
            serviceUUID = TextUtils.isEmpty(serviceUUID) ? config.getServiceUUID() : serviceUUID;
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

            bluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(filters, settings, bleScanCallback);
            mEventListenerCallback.onEvent(SCANNING_STATUS, true);
        }
    }

    public void stopScan() {
        bluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(bleScanCallback);
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
            byte[] bytePK = result.getScanRecord().getServiceData(pUuid);

            String pk = bytePK != null ? new String(bytePK, Charset.forName("UTF-8")) : "NaN";

            int tx = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                tx = result.getTxPower();
            }
            super.onScanResult(callbackType, result);
            handleScanResults(result, pk, tx);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode != SCAN_FAILED_ALREADY_STARTED) {
                //scanningStatus
//                BLEManager.getInstance(context).onEvent(SCANNING_STATUS,false);
                mEventListenerCallback.onEvent(SCANNING_STATUS,false);
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
//                BLEManager.getInstance(context).onEvent(FOUND_DEVICE,newDevice);
                mEventListenerCallback.onEvent(FOUND_DEVICE,newDevice);

                // handle scans
                Scan newScan = new Scan(System.currentTimeMillis(),
                        pk,
                        result.getDevice().getAddress(),
                        BLEManager.BLEProtocol.GAP.toString(),
                        result.getRssi(),
                        tx);
                dbClient.addScan(newScan);
//                BLEManager.getInstance(context).onEvent(SCANNING_STATUS, true);
                mEventListenerCallback.onEvent(SCANNING_STATUS, true);
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