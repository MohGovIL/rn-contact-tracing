package com.wix.specialble.bt;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

@Entity (tableName = "scans")
public class Scan {
    @PrimaryKey (autoGenerate = true)
    private int id;

    @NonNull
    private String publicKey = "";

    @ColumnInfo(name = "timestamp")
    private long timestamp = 0;

    @ColumnInfo(name = "scan_address")
    private String scanAddress = "";

    @ColumnInfo(name = "rssi")
    private int rssi = 0;

    @ColumnInfo(name = "tx")
    private int tx = 0;

    @ColumnInfo(name = "scan_protocol")
    private String scanProtocol = "";

    public Scan() {
    }

    public Scan(long timestamp, String key, String address, String protocol, int rssi, int tx ) {
        this.timestamp = timestamp;
        publicKey = key==null ? "NaN" : key;
        scanAddress = address==null ? "NaN" : address;
        scanProtocol = protocol==null ? "NaN" : protocol;
        this.rssi = rssi;
        this.tx = tx;
    }

    public String getPublicKey() {
        return publicKey;
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

    public String getScanAddress() {
        return scanAddress;
    }

    public void setScanAddress(String scanAddress) {
        this.scanAddress = scanAddress;
    }

    public String getScanProtocol() {
        return scanProtocol;
    }

    public void setScanProtocol(String scanProtocol) {
        this.scanProtocol = scanProtocol;
    }

    public WritableMap toWritableMap() {
        WritableMap scanWritableMap = Arguments.createMap();
        scanWritableMap.putInt("scan_id", id);
        scanWritableMap.putDouble("scan_timestamp", timestamp);
        scanWritableMap.putString("public_key", publicKey);
        scanWritableMap.putString("scan_address", scanAddress);
        scanWritableMap.putInt("scan_rssi", rssi);
        scanWritableMap.putInt("scan_tx", tx);
        scanWritableMap.putString("scan_protocol", scanProtocol);
        return scanWritableMap;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTx() {
        return tx;
    }

    public void setTx(int tx) {
        this.tx = tx;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
