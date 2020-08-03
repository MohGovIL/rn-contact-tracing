package moh.gov.il.crypto;

import org.json.JSONObject;

/**
 * Created by hagai on 11/05/2020.
 */
public class Match {

    private Contact mContact;
    private byte[] mInfectedGeohash;
    private byte[] mProof;
    private byte[] mEpochKey;
    private int mInfectedTime;
    /**
     *
     * @param contact - Contact with the other user.
     * @param ephidGeohash - Other user ephemeral id.
     * @param ephidUserRand - Other user proof.
     * @param otherTime - Other user epoch time of contact.
     * @param otherUnit - Other user time unit.
     */
    public Match(Contact contact, byte[] ephidGeohash, byte[] ephidUserRand, Time otherTime, int otherUnit, byte[] epochKey ) {

        this.mContact = contact;
        this.mInfectedGeohash = ephidGeohash;
        this.mProof = ephidUserRand;
        this.mEpochKey = epochKey;
        // Up to T_UNIT
        this.mInfectedTime = otherTime.getDay() * Time.DAY + otherTime.getEpoch() * Time.EPOCH + otherUnit * Time.UNIT;

    }

    public Contact getContact() { return mContact; }

    public byte[] getInfectionGeohash() { return mInfectedGeohash; }

    public byte[] getProof() { return mProof; }

    public int getInfectedTime() { return mInfectedTime; }

    public JSONObject toJsonObject()
    {
        return mContact.toJson();
    }

    public byte[] getmEpochKey() {
        return mEpochKey;
    }
}
