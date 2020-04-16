package com.wix.specialble.bt;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Device {

    @NonNull
    @PrimaryKey
    private String publicKey = "";

    @ColumnInfo(name = "device_name")
    private String deviceName = "";

    @ColumnInfo(name = "device_address")
    private String deviceAddress = "";

    @ColumnInfo(name = "device_data")
    private String deviceData = "";

    @ColumnInfo(name = "rssi")
    private int rssi = 0;
    //const device1 = {device_name: 'aaa', device_address: '12:342:543:546', device_data: data,device_rssi: '333'}

    public Device() {
    }

    public Device(String key, String name, String address, String data, int rssi) {
        publicKey = key == null ? "NaN" : key;
        deviceName = name == null ? "NaN" : name;
        deviceData = data == null ? "NaN" : data;
        deviceAddress = address == null ? "NaN" : address;
        this.rssi = rssi;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceData() {
        return deviceData;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setDeviceData(String deviceData) {
        this.deviceData = deviceData;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
        deviceWritableMap.putString("device_name", deviceName);
        deviceWritableMap.putString("device_address", deviceAddress);
        deviceWritableMap.putString("device_data", deviceData);
        deviceWritableMap.putInt("device_rssi", rssi);
        return deviceWritableMap;
    }


    public void fromJSONString(String jsonString) {
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            publicKey = jsonObj.getString("public_key");
            deviceName = jsonObj.getString("device_name");
            deviceAddress = jsonObj.getString("device_address");
            deviceData = jsonObj.getString("device_data");
            rssi = jsonObj.getInt("device_rssi");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toJSONString() {
        JSONObject item = new JSONObject();
        try {
            item.put("public_key", publicKey);
            item.put("device_name", deviceName);
            item.put("device_address", deviceAddress);
            item.put("device_data", deviceData);
            item.put("device_rssi", rssi);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item.toString();
    }
}
