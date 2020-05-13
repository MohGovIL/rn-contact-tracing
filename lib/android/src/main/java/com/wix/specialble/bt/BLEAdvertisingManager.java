package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.wix.crypto.CryptoManager;
import com.wix.specialble.config.Config;
import com.wix.specialble.listeners.IEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;

public class BLEAdvertisingManager {

    public static final String ADVERTISING_STATUS = "advertisingStatus";
    Context mContext;
    BluetoothLeAdvertiser advertiser;
    private String TAG = "BLEAdvertisingManager";
    private IEventListener mEventListenerCallback;


    AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            mEventListenerCallback.onEvent(BLEAdvertisingManager.ADVERTISING_STATUS, true);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            mEventListenerCallback.onEvent(ADVERTISING_STATUS, errorCode == ADVERTISE_FAILED_ALREADY_STARTED);
            Log.d(TAG, "onAdvertiseStartFailed - ErrorCode: " + errorCode);
        }
    };

    BLEAdvertisingManager(Context context, IEventListener eventListenerCallback) {
        mContext = context;
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        mEventListenerCallback = eventListenerCallback;
    }

    public void stopAdvertise() {
        advertiser.stopAdvertising(advertiseCallback);
        mEventListenerCallback.onEvent(ADVERTISING_STATUS, false);
    }

    public void startAdvertise(String serviceUUID) {
        Config config = Config.getInstance(mContext);

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(serviceUUID));

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(pUuid);
        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.setIncludeTxPowerLevel(true);

        int currentTime = (int)(System.currentTimeMillis() / 1000);
        byte[] key = CryptoManager.getInstance(mContext).mySelf.generateEphemeralId(currentTime, BLEScannerManager.sGeoHash);

        dataBuilder.addServiceData(pUuid, key);

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(config.getAdvertiseMode());
        settingsBuilder.setTimeout((int) config.getAdvertiseDuration());
        settingsBuilder.setTxPowerLevel(config.getAdvertiseTXPowerLevel());
        settingsBuilder.setConnectable(false);

        advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
    }
}