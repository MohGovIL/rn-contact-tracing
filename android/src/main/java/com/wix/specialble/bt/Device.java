package com.wix.specialble.bt;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Device {

    @NonNull
    @PrimaryKey
    private String publicKey = "";

    @ColumnInfo(name = "device_address")
    private String deviceAddress = "";

    @ColumnInfo(name = "rssi")
    private int rssi = 0;

    public Device() {
    }

    public Device(String key, String address, int rssi ) {
        publicKey = key==null ? "NaN" : key;
        deviceAddress = address==null ? "NaN" : address;
        this.rssi = rssi;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }


    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getRssi() {
        return rssi;
    }


    public WritableMap toWritableMap() {
        WritableMap deviceWritableMap = Arguments.createMap();
        deviceWritableMap.putString("public_key", publicKey);
        deviceWritableMap.putString("device_address", deviceAddress);
        deviceWritableMap.putInt("device_rssi", rssi);
        return deviceWritableMap;
    }


    public void fromJSONString(String jsonString) {
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            publicKey = jsonObj.getString("public_key");
            deviceAddress = jsonObj.getString("device_address");
            rssi = jsonObj.getInt("device_rssi");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toJSONString() {
        JSONObject item = new JSONObject();
        try {
            item.put("public_key", publicKey);
            item.put("device_address", deviceAddress);
            item.put("device_rssi", rssi);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item.toString();
    }
}
