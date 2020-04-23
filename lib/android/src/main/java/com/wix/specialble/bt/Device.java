package com.wix.specialble.bt;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

@Entity
public class Device {

    @NonNull
    @PrimaryKey
    private String publicKey = "";

    @ColumnInfo(name = "first_timestamp")
    private long firstTimestamp = 0;

    @ColumnInfo(name = "last_timestamp")
    private long lastTimestamp = 0;

    @ColumnInfo(name = "device_address")
    private String deviceAddress = "";

    @ColumnInfo(name = "rssi")
    private int rssi = 0;

    @ColumnInfo(name = "tx")
    private int tx = 0;

    @ColumnInfo(name = "device_protocol")
    private String deviceProtocol = "";

    public Device() {
    }

    public Device(long firstTimestamp, long lastTimestamp, String key, String address, String protocol, int rssi, int tx ) {
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
        publicKey = key==null ? "NaN" : key;
        deviceAddress = address==null ? "NaN" : address;
        deviceProtocol = protocol==null ? "NaN" : protocol;
        this.rssi = rssi;
        this.tx = tx;
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

    public String getDeviceProtocol() {
        return deviceProtocol;
    }

    public void setDeviceProtocol(String deviceProtocol) {
        this.deviceProtocol = deviceProtocol;
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
        deviceWritableMap.putDouble("device_first_timestamp", firstTimestamp);
        deviceWritableMap.putDouble("device_last_timestamp", lastTimestamp);
        deviceWritableMap.putString("public_key", publicKey);
        deviceWritableMap.putString("device_address", deviceAddress);
        deviceWritableMap.putInt("device_rssi", rssi);
        deviceWritableMap.putInt("device_tx", tx);
        deviceWritableMap.putString("device_protocol", deviceProtocol);
        return deviceWritableMap;
    }

    public int getTx() {
        return tx;
    }

    public void setTx(int tx) {
        this.tx = tx;
    }

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public void setFirstTimestamp(long firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
}
