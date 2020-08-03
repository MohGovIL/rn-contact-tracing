package moh.gov.il.crypto.key;

import moh.gov.il.crypto.custom.Triplet;

import java.util.List;

/**
 * Created by hagai on 11/05/2020.
 *  Represents a set of keys an infected user sends to the server.
 */



public class UserKey {

    private byte[] mUserId;
    private byte[] mKeyId;
    private byte[] mKeyMasterVerification;

    List<Triplet<Integer, Integer, byte[]>> mPreEpoch;

    /**
     * Constructor
     * @param userId - user id.
     * @param keyId - Identification key.
     * @param keyMasterVerification - key for validating user.
     * @param preEpoch - cryptographic keys for users to check for contact.
     */
    public UserKey(byte[] userId, byte[] keyId, List<Triplet<Integer, Integer, byte[]>> preEpoch, byte[] keyMasterVerification) {

        this.mUserId = userId;
        this.mKeyId = keyId;
        this.mKeyMasterVerification = keyMasterVerification;
        this.mPreEpoch = preEpoch;
    }

    public byte[] getUserId(){ return mUserId; }

    public byte[] getKeyId() { return mKeyId; }

    public byte[] getKeyMasterVerification() { return mKeyMasterVerification; }

    public List<Triplet<Integer, Integer, byte[]>> getPreEpoch() { return mPreEpoch; }
}

