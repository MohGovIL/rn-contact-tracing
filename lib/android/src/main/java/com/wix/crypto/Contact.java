package com.wix.crypto;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by hagai on 11/05/2020.
 */
@Entity(tableName = "contact", indices = {@Index(value = {"EphemeralID"},unique = true)})
public class Contact{

    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name = "Id")
    private int id;

    @NonNull
    @ColumnInfo(name = "EphemeralID")
    private byte[] mEphID;

    @ColumnInfo(name = "Rssi")
    private byte[] mRSSI;

    @NonNull
    @ColumnInfo(name = "Received_time")
    private int mTime;

    @ColumnInfo(name = "Location")
    private byte[] mLocation;

    @ColumnInfo(name = "User_Id")
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

        this.mEphID = ephemeralId;
        this.mRSSI = rssi;
        this.mTime = time;
        this.mLocation = location;
    }

    @Override
    public boolean equals(Object obj) {

        Contact other = (Contact)obj;
        if(mEphID == other.mEphID               &&
                mRSSI == other.mRSSI                 &&
                mTime == other.mTime                 &&
                mLocation == other.mLocation )
            return true;

        return false;
    }

    public byte[] getEphID() { return mEphID; }

    public byte[] getRSSI() { return mRSSI; }

    public int getTime() { return mTime; }

    public byte[] getLocation() { return mLocation; }

}