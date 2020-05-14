package com.wix.crypto.key;

import com.google.gson.annotations.SerializedName;
import com.wix.crypto.Crypto;
import com.wix.crypto.utilities.Hex;
import com.wix.crypto.utilities.BytesUtils;
import com.wix.crypto.utilities.DerivationUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hagai on 11/05/2020.
 */
public class EpochKey {

    @SerializedName("PreKey")
    private byte[] mPreKey;
    @SerializedName("EpochKey")
    private byte[] mEpochKey;
    @SerializedName("EpochEnc")
    private byte[] mEpochEnc;
    @SerializedName("EpochMac")
    private byte[] mEpochMac;
    @SerializedName("EpochVer")
    private byte[] mEpochVer;

    /**
     *
     * @param day - epoch day.
     * @param epochKey - epoch index.
     * @param mDayKey - day key.
     */
    public EpochKey(int day, int epochIndex, DayKey mDayKey) {

        byte[] timePrefix = BytesUtils.byteConcatenation(BytesUtils.numToBytes(day, 4),
                BytesUtils.numToBytes(epochIndex, 1));

        byte[] zeroByteEleven = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] zeroByteTen = new byte[]    {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] preKeyMessage = BytesUtils.byteConcatenation(timePrefix, zeroByteEleven);

        mPreKey = Crypto.AES(mDayKey.getDayKey(), preKeyMessage);

        mEpochKey = DerivationUtils.getKeyEpoch(mPreKey, mDayKey.getDayCommitKey(), BytesUtils.numToBytes(day, 4), BytesUtils.numToBytes(epochIndex, 1));

        byte[] epochEncMessage = BytesUtils.byteConcatenation(timePrefix, zeroByteEleven);
        mEpochEnc = Crypto.AES(mEpochKey, epochEncMessage);

        byte[] epochMACmessage = BytesUtils.byteConcatenation(timePrefix, zeroByteTen);
        mEpochMac = Crypto.AES(mEpochKey, epochMACmessage);

        byte[] epochVerMessage = BytesUtils.byteConcatenation(timePrefix, zeroByteEleven);
        mEpochVer = Crypto.AES(mDayKey.getDayVerificationKey(), epochVerMessage);

    }


    public JSONObject toJson() throws JSONException
    {
        JSONObject jo = new JSONObject();
        jo.put("PreKey", Hex.toHexString(mPreKey));
        jo.put("EpochKey", Hex.toHexString(mEpochKey));
        jo.put("EpochEnc", Hex.toHexString(mEpochEnc));
        jo.put("EpochMac", Hex.toHexString(mEpochMac));
        jo.put("EpochVer", Hex.toHexString(mEpochVer));
        return jo;
    }

    private EpochKey(byte[] pre, byte[] key, byte[] enc, byte[] mac, byte[] ver)
    {
        mPreKey = pre;
        mEpochKey = key;
        mEpochEnc = enc;
        mEpochMac = mac;
        mEpochVer = ver;
    }

    public static EpochKey fromJson(JSONObject jo) throws JSONException
    {
        return new EpochKey
        (
                Hex.fromHexString(jo.getString("PreKey")),
                Hex.fromHexString(jo.getString("EpochKey")),
                Hex.fromHexString(jo.getString("EpochEnc")),
                Hex.fromHexString(jo.getString("EpochMac")),
                Hex.fromHexString(jo.getString("EpochVer"))
        );
    }




    public byte[] getPreKey() { return mPreKey; }

    public byte[] getEpochKey() { return mEpochKey; }

    public byte[] getEpochEncKey() { return mEpochEnc; }

    public byte[] getEpocMacKey () { return mEpochMac; }

    public byte[] getEpochVerKey() { return mEpochVer; }
}