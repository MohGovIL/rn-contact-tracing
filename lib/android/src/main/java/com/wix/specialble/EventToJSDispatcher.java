package com.wix.specialble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Scan;
import com.wix.specialble.listeners.IEventListener;

public class EventToJSDispatcher implements IEventListener {
    ReactApplicationContext context;
    static EventToJSDispatcher sEventDispatcher;

    private EventToJSDispatcher(ReactApplicationContext context) {
        this.context = context;
    }

    public static EventToJSDispatcher getInstance(ReactApplicationContext context) {
        if (sEventDispatcher == null) {
            sEventDispatcher = new EventToJSDispatcher(context);
        }
        return sEventDispatcher;
    }

    private void dispatch(@NonNull String eventName, @Nullable Object data) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
    }

    @Override
    public void onEvent(String event, Object data) {
        if (data instanceof Boolean) {
            dispatch(event, toBoolean((Boolean) data));
        } else if (data instanceof WritableMap) {
            dispatch(event, data);
        }
    }

    // returns true or false if false or null
    private boolean toBoolean(Boolean bool) {
        return bool != null && bool;
    }

    public void sendAdvertisingStatus(boolean status) {
        dispatch("advertisingStatus",status);
    }

    public void sendScanningStatus(boolean status) {
        dispatch("scanningStatus",status);
    }

    public void sendNewDevice(Device newDevice) {
        WritableMap params = newDevice.toWritableMap();
        dispatch("foundDevice",params);
    }

    public void sendNewScan(Scan newScan) {
        WritableMap params = newScan.toWritableMap();
        dispatch("foundScan",params);
    }
}
