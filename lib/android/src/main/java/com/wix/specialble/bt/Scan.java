package com.wix.specialble.bt;

import android.util.Log;

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

    @ColumnInfo(name = "proximity")
    private float proximityValue;

    @ColumnInfo(name = "acceleration_x")
    private float accelerationX;

    @ColumnInfo(name = "acceleration_y")
    private float accelerationY;

    @ColumnInfo(name = "acceleration_z")
    private float accelerationZ;

    @ColumnInfo(name = "rotation_x")
    private float rotationVectorX;

    @ColumnInfo(name = "rotation_y")
    private float rotationVectorY;

    @ColumnInfo(name = "rotation_z")
    private float rotationVectorZ;

    @ColumnInfo(name = "rotation_scalar")
    private float rotationVectorScalar;

    @ColumnInfo(name = "battery_level")
    private int batteryLevel;

    public Scan() {
    }

    public Scan(long timestamp, String scannedToken, String address, String protocol, int rssi, int tx, float proximity, float[] accelerometerValues, float[] rotationVectorValues, int batteryLevel) {
        this.timestamp = timestamp;
        publicKey = scannedToken == null ? "NaN" : scannedToken;
        scanAddress = address == null ? "NaN" : address;
        scanProtocol = protocol == null ? "NaN" : protocol;
        this.rssi = rssi;
        this.tx = tx;
        this.proximityValue = proximity;
        this.accelerationX = accelerometerValues[0];
        this.accelerationY = accelerometerValues[1];
        this.accelerationZ = accelerometerValues[2];
        this.rotationVectorX = rotationVectorValues[0];
        this.rotationVectorY = rotationVectorValues[1];
        this.rotationVectorZ = rotationVectorValues[2];
        this.rotationVectorScalar = rotationVectorValues[3];
        this.batteryLevel = batteryLevel;
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
        scanWritableMap.putDouble("proximity", proximityValue);
        scanWritableMap.putDouble("acceleration_x", accelerationX);
        scanWritableMap.putDouble("acceleration_y", accelerationY);
        scanWritableMap.putDouble("acceleration_z", accelerationZ);
        scanWritableMap.putDouble("rotation_x", rotationVectorX);
        scanWritableMap.putDouble("rotation_y", rotationVectorY);
        scanWritableMap.putDouble("rotation_z", rotationVectorZ);
        scanWritableMap.putDouble("rotation_scalar", rotationVectorScalar);
        scanWritableMap.putDouble("battery", batteryLevel);
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

    public float getProximityValue() {
        return proximityValue;
    }

    public void setProximityValue(float proximityValue) {
        this.proximityValue = proximityValue;
    }

    public float getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(float accelerationX) {
        this.accelerationX = accelerationX;
    }

    public float getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(float accelerationY) {
        this.accelerationY = accelerationY;
    }

    public float getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(float accelerationZ) {
        this.accelerationZ = accelerationZ;
    }

    public float getRotationVectorX() {
        return rotationVectorX;
    }

    public void setRotationVectorX(float rotationVectorX) {
        this.rotationVectorX = rotationVectorX;
    }

    public float getRotationVectorY() {
        return rotationVectorY;
    }

    public void setRotationVectorY(float rotationVectorY) {
        this.rotationVectorY = rotationVectorY;
    }

    public float getRotationVectorZ() {
        return rotationVectorZ;
    }

    public void setRotationVectorZ(float rotationVectorZ) {
        this.rotationVectorZ = rotationVectorZ;
    }

    public float getRotationVectorScalar() {
        return rotationVectorScalar;
    }

    public void setRotationVectorScalar(float rotationVectorScalar) {
        this.rotationVectorScalar = rotationVectorScalar;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
