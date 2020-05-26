package com.wix.specialble.bt;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "events")
public class Event {

    @NonNull
    @PrimaryKey (autoGenerate = true)
    private int id;

    @ColumnInfo(name = "timestamp")
    private long timestamp = 0;

    @ColumnInfo(name = "device_name")
    private String deviceName  = "";

    @ColumnInfo(name = "action_type")
    private String actionType = "";

    @ColumnInfo(name = "success")
    private String success = "";

    @ColumnInfo(name = "errorMessage")
    private String errorMessage = "";

    public Event(long timestamp, String deviceName, String actionType, String success, String errorMessage) {
        this.timestamp = timestamp;
        this.deviceName = deviceName;
        this.actionType = actionType;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getActionType() {
        return actionType;
    }

    public String getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}