package com.wix.crypto;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by hagai on 11/05/2020.
 */
@Entity(tableName = "Contacts", indices = {@Index(value = {"ephemeral_id"},unique = true)})
public class Contact{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @NonNull
    @ColumnInfo(name = "ephemeral_id")
    private byte[] ephemeral_id;

    @ColumnInfo(name = "rssi")
    private byte[] rssi;

    @NonNull
    @ColumnInfo(name = "timestamp")
    private int timestamp;

    @ColumnInfo(name = "geohash")
    private byte[] geohash;

    @ColumnInfo(name = "user_id")
    private byte[] user_id;

    /**
     * A contact which was sent the user a BLE message.
     *
     * @param ephemeralId - The contact ephemeral id.
     * @param rssi - Good question. my name contains covid but I don't know everything(covid6Pi).
     * @param time - Time of contact as recorded by the receiving user.
     * @param location - Location of user contact when BLE message received.
     */
    public Contact(byte[] ephemeralId, byte[] rssi, int time, byte[] location) {

        assert ephemeralId.length == Constants.MESSAGE_LEN;

        this.ephemeral_id = ephemeralId;
        this.rssi = rssi;
        this.timestamp = time;
        this.geohash = location;
//        this.user_id = user_id;
    }

    public Contact(){}

    @Override
    public boolean equals(Object obj) {

        Contact other = (Contact)obj;
        if(ephemeral_id == other.ephemeral_id &&
                rssi == other.rssi &&
                timestamp == other.timestamp &&
                geohash == other.geohash)
            return true;

        return false;
    }

    @NonNull
    public byte[] getEphemeral_id() { return ephemeral_id; }

    public byte[] getRssi() { return rssi; }

    public int getTimestamp() { return timestamp; }

    public byte[] getGeohash() { return geohash; }

    public byte[] getUser_id() { return user_id; }

    public void setEphemeral_id(@NonNull byte[] mEphID) {
        this.ephemeral_id = mEphID;
    }

    public void setRssi(byte[] mRSSI) {
        this.rssi = mRSSI;
    }

    public void setTimestamp(int mTime) {
        this.timestamp = mTime;
    }

    public void setGeohash(byte[] mLocation) {
        this.geohash = mLocation;
    }

    public void setUser_id(byte[] user_id) {
        this.user_id = user_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}