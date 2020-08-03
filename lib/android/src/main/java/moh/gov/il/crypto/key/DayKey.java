package moh.gov.il.crypto.key;

import moh.gov.il.crypto.utilities.BytesUtils;
import moh.gov.il.crypto.utilities.DerivationUtils;

/**
 * Created by hagai on 11/05/2020.
 */
public class DayKey {

    private byte[] mDayKey;
    private byte[] mDayVerificationKey;
    private byte[] mDayCommitmentKey;

    /**
     * Day key to derive epoch keys from.
     *
     * @param day - The corresponding day.
     * @param masterKey - The current day master key. Use get_next_master_key.
     * @param masterVerification - The master commitment key.
     * @param masterCommitment - Master verification key for proofing id.
     */
    public DayKey(int day, byte[] masterKey, byte[] masterVerification, byte[] masterCommitment) {

        mDayKey = DerivationUtils.getKeyForDayI(masterKey);
        mDayVerificationKey = DerivationUtils.getKeyVerificationForDayI(masterVerification, day);
        mDayCommitmentKey = DerivationUtils.getKeyCommitmentForDayI(masterCommitment, BytesUtils.numToBytes(day, 4));
    }

    public byte[] getDayKey() { return mDayKey; }

    public byte[] getDayCommitKey() { return mDayCommitmentKey; }

    public byte[] getDayVerificationKey() { return mDayVerificationKey; }
}