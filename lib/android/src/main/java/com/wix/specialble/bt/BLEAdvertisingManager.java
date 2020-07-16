package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Log;

import com.wix.crypto.CryptoManager;
import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.listeners.IEventListener;
import com.wix.specialble.sensor.SensorUtils;
import com.wix.specialble.util.Constants;
import java.util.UUID;

public class BLEAdvertisingManager {

    public static final String ADVERTISING_STATUS = "advertisingStatus";
    private BluetoothAdapter bluetoothAdapter;
    Context mContext;
    BluetoothLeAdvertiser advertiser;
    private String TAG = "BLEAdvertisingManager";
    private IEventListener mEventListenerCallback;


    AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            
            insertToDb(new Event(System.currentTimeMillis(), Config.getInstance(mContext).getToken(), Constants.ACTION_ADVERTISE, Constants.ADVERTISE_SUCCESS, "", SensorUtils.getBatteryPercentage(mContext)));

            mEventListenerCallback.onEvent(BLEAdvertisingManager.ADVERTISING_STATUS, true);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            insertToDb(new Event(System.currentTimeMillis(), Config.getInstance(mContext).getToken(), Constants.ACTION_ADVERTISE, Constants.ADVERTISE_FAIL, "error code: " + errorCode, SensorUtils.getBatteryPercentage(mContext)));

            mEventListenerCallback.onEvent(ADVERTISING_STATUS, errorCode == ADVERTISE_FAILED_ALREADY_STARTED);
            Log.d(TAG, "onAdvertiseStartFailed - ErrorCode: " + errorCode);
        }
    };

    private void insertToDb(final Event event) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DBClient.getInstance(mContext).insert(event);
            }
        });
    }

    BLEAdvertisingManager(Context context, IEventListener eventListenerCallback) {
        mContext = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        mEventListenerCallback = eventListenerCallback;
    }

    public void stopAdvertise() {
        if(bluetoothAdapter.isEnabled()){

            if(advertiser == null)
                advertiser = bluetoothAdapter.getBluetoothLeAdvertiser(); // if we turned the bluetooth on while the service is running

            if(advertiser != null)
                advertiser.stopAdvertising(advertiseCallback);

            mEventListenerCallback.onEvent(ADVERTISING_STATUS, false);
        }
    }

    public void startAdvertise(String serviceUUID) {
        if(bluetoothAdapter.isEnabled()) {

            if(advertiser == null)
                advertiser = bluetoothAdapter.getBluetoothLeAdvertiser(); // if we turned the bluetooth on while the service is running

            Config config = Config.getInstance(mContext);

            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(serviceUUID));

            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            dataBuilder.addServiceUuid(pUuid);
            dataBuilder.setIncludeDeviceName(false);
            dataBuilder.setIncludeTxPowerLevel(true);

            int currentTime = (int) (System.currentTimeMillis() / 1000);
            byte[] key = CryptoManager.getInstance(mContext).mySelf.generateEphemeralId(currentTime, BLEScannerManager.sGeoHash);

            dataBuilder.addServiceData(pUuid, key);

            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(config.getAdvertiseMode());
            settingsBuilder.setTimeout((int) config.getAdvertiseDuration());
            settingsBuilder.setTxPowerLevel(config.getAdvertiseTXPowerLevel());
            settingsBuilder.setConnectable(true);

            if(advertiser != null)
                advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
        }
    }
}