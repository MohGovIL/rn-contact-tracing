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
    private byte[] ephemeralId;

    @ColumnInfo(name = "rssi")
    private byte[] RSSI;

    @NonNull
    @ColumnInfo(name = "timestamp")
    private int Time;

    @ColumnInfo(name = "geohash")
    private byte[] Location;

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

        this.ephemeralId = ephemeralId;
        this.RSSI = rssi;
        this.Time = time;
        this.Location = location;
//        this.user_id = user_id;
    }

    public Contact(){}

    @Override
    public boolean equals(Object obj) {

        Contact other = (Contact)obj;
        if(ephemeralId == other.ephemeralId &&
                RSSI == other.RSSI &&
                Time == other.Time &&
                Location == other.Location)
            return true;

        return false;
    }

    public byte[] getEphemeralId() { return ephemeralId; }

    public byte[] getRSSI() { return RSSI; }

    public int getTime() { return Time; }

    public byte[] getLocation() { return Location; }

    public byte[] getUser_id() { return user_id; }

    public void setEphemeralId(@NonNull byte[] mEphID) {
        this.ephemeralId = mEphID;
    }

    public void setRSSI(byte[] mRSSI) {
        this.RSSI = mRSSI;
    }

    public void setTime(int mTime) {
        this.Time = mTime;
    }

    public void setLocation(byte[] mLocation) {
        this.Location = mLocation;
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