package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.wix.specialble.EventToJSDispatcher;

import java.nio.charset.Charset;
import java.util.UUID;

public class BLEAdvertisingManager {

    private static BLEAdvertisingManager sAdvertisingManager;
    ReactApplicationContext context;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeAdvertiser advertiser;
    private String TAG = "BLEAdvertisingManager";


    AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            EventToJSDispatcher.getInstance(context).sendAdvertisingStatus(true);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            EventToJSDispatcher.getInstance(context).sendAdvertisingStatus((errorCode == ADVERTISE_FAILED_ALREADY_STARTED));
            Log.d(TAG, "onAdvertiseStartFailed - ErrorCode: " + errorCode);
        }
    };

    private BLEAdvertisingManager(ReactApplicationContext context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        advertiser = bluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

    }

    public static BLEAdvertisingManager getInstance(ReactApplicationContext context, BluetoothAdapter bluetoothAdapter) {
        if (sAdvertisingManager != null) {
            return sAdvertisingManager;
        }
        return new BLEAdvertisingManager(context, bluetoothAdapter);
    }


    public void stopAdvertise() {
        advertiser.stopAdvertising(advertiseCallback);
        EventToJSDispatcher.getInstance(context).sendAdvertisingStatus(false);
    }

    public void startAdvertise(String serviceUUID, String publicKey) {

        byte[] testString = publicKey.getBytes();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(serviceUUID));

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(pUuid);
        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addManufacturerData(1023, testString);
        dataBuilder.addServiceData(pUuid, publicKey.getBytes(Charset.forName("UTF-8")));
        dataBuilder.build();

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTimeout(180000);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.build();


        advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);


    }
}
