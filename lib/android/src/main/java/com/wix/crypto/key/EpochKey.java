package com.wix.crypto.key;

import com.wix.crypto.Crypto;
import com.wix.crypto.utilities.BytesUtils;
import com.wix.crypto.utilities.DerivationUtils;

/**
 * Created by hagai on 11/05/2020.
 */
public class EpochKey {

    private byte[] mPreKey;
    private byte[] mEpochKey;
    private byte[] mEpochEnc;
    private byte[] mEpochMac;
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

    public byte[] getPreKey() { return mPreKey; }

    public byte[] getEpochKey() { return mEpochKey; }

    public byte[] getEpochEncKey() { return mEpochEnc; }

    public byte[] getEpocMacKey () { return mEpochMac; }

    public byte[] getEpochVerKey() { return mEpochVer; }
}