package com.wix.crypto;

/**
 * Created by hagai on 11/05/2020.
 */
public class Match {

    private Contact mContact;
    private byte[] mInfectedGeohash;
    private byte[] mProof;
    private int mInfectedTime;

    /**
     *
     * @param contact - Contact with the other user.
     * @param ephidGeohash - Other user ephemeral id.
     * @param ephidUserRand - Other user proof.
     * @param otherTime - Other user epoch time of contact.
     * @param otherUnit - Other user time unit.
     */
    public Match(Contact contact, byte[] ephidGeohash, byte[] ephidUserRand, Time otherTime, int otherUnit ) {

        this.mContact = contact;
        this.mInfectedGeohash = ephidGeohash;
        this.mProof = ephidUserRand;
        // Up to T_UNIT
        this.mInfectedTime = otherTime.getDay() * Time.DAY + otherTime.getEpoch() * Time.EPOCH + otherUnit * Time.UNIT;

    }

    public Contact getContact() { return mContact; }

    public byte[] getInfectionGeohash() { return mInfectedGeohash; }

    public byte[] getProof() { return mProof; }

    public int getInfectedTime() { return mInfectedTime; }
}
