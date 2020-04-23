package com.wix.specialble.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.wix.specialble.config.Config;
import com.wix.specialble.listeners.IEventListener;

import java.nio.charset.Charset;
import java.util.UUID;

public class BLEAdvertisingManager {

    public static final String ADVERTISING_STATUS = "advertisingStatus";
    Context mContext;
    BluetoothAdapter bluetoothAdapter;
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
//            BLEManager.getInstance(context).onEvent(ADVERTISING_STATUS, errorCode == ADVERTISE_FAILED_ALREADY_STARTED);
            Log.d(TAG, "onAdvertiseStartFailed - ErrorCode: " + errorCode);
        }
    };

    BLEAdvertisingManager(Context context, BluetoothAdapter bluetoothAdapter, IEventListener eventListenerCallback) {
        mContext = context;
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        mEventListenerCallback = eventListenerCallback;

    }

    public void stopAdvertise() {
        advertiser.stopAdvertising(advertiseCallback);
        mEventListenerCallback.onEvent(ADVERTISING_STATUS, false);
//        BLEManager.getInstance(context).onEvent(ADVERTISING_STATUS, false);
//        EventToJSDispatcher.getInstance(context).sendAdvertisingStatus(false);
    }

    public void startAdvertise(String serviceUUID, String publicKey) {
        Config config = Config.getInstance(mContext);

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(serviceUUID));

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(pUuid);
        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceData(pUuid, publicKey.getBytes(Charset.forName("UTF-8")));

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(config.getAdvertiseMode());
        settingsBuilder.setTimeout((int) config.getAdvertiseDuration());
        settingsBuilder.setTxPowerLevel(config.getAdvertiseTXPowerLevel());
        settingsBuilder.setConnectable(false);
        advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
    }
}