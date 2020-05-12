package com.wix.crypto.utilities;

import com.wix.crypto.Constants;
import com.wix.crypto.Crypto;
import com.wix.crypto.custom.Pair;

import java.util.Arrays;

/**
 * Created by hagai on 11/05/2020.
 */
public class DerivationUtils {

    public static final byte[] getKeyId(byte[] masterKey) {

        byte[] message = Constants.ENCODED_STRINGS.get("id");
        byte[] result = Crypto.HMAC(masterKey, message);

        return Arrays.copyOf(result, Constants.KEY_LEN);

    }

    public static final byte[] getMasterKeyCommitment(byte[] keyId, byte[] userId) {

        byte[] message = BytesUtils.byteConcatination(userId, Constants.ENCODED_STRINGS.get("idCom"));
        byte[ ] keyMasterCommitment = Crypto.HMAC(keyId, message);

        return Arrays.copyOf(keyMasterCommitment, Constants.KEY_LEN);
    }

    public static byte[] getKeyMasterVerification(byte[] masterKey) {

        byte[] message = Constants.ENCODED_STRINGS.get("verifkey");
        byte[] keyMasterVerification = Crypto.HMAC(masterKey, message);

        return Arrays.copyOf(keyMasterVerification, Constants.KEY_LEN);
    }

    public static byte[] getNextDayMasterKey(byte[] masterKey, boolean isInstallDay) {


        byte[] message;
        if(isInstallDay){
            message = Constants.ENCODED_STRINGS.get("master0");
        }
        else {
            message = Constants.ENCODED_STRINGS.get("master");
        }
        byte[] dayMasterKey = Crypto.HMAC(masterKey, message);


        return Arrays.copyOf(dayMasterKey, Constants.KEY_LEN);
    }

    public static byte[] getKeyEpoch(byte[] preKey, byte[] commitKey, byte[] day, byte[] epoch) {

        byte[] messagePart1 = BytesUtils.byteConcatination(commitKey, day);
        byte[] messagePart2 = BytesUtils.byteConcatination(epoch, Constants.ENCODED_STRINGS.get("depoch"));
        byte[] message = BytesUtils.byteConcatination(messagePart1, messagePart2);

        byte[] epochKey = Crypto.HMAC(preKey, message);

        return Arrays.copyOf(epochKey, Constants.KEY_LEN);
    }

    public static byte[] getKeyCommitmentForDayI(byte[] keyMasterCommitment, byte[] day) {
        assert day.length == 4;

        byte[] zeroBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] message = BytesUtils.byteConcatination(day, zeroBytes);

        return Crypto.AES(keyMasterCommitment, message);
    }

    public static byte[] getKeyVerificationForDayI(byte[] keyMasterVerification, int dayI) {

        byte[] day = BytesUtils.numToBytes(dayI, 4);
        byte[] message = BytesUtils.byteConcatination(day, Constants.ENCODED_STRINGS.get("dverif"));

        byte[] result = Crypto.HMAC(keyMasterVerification, message);

        return Arrays.copyOf(result, Constants.KEY_LEN);
    }

    //TODO:: problem with Pair data type.
    public static Pair<byte[],byte[]> getEpochKeys(byte[] epochKey, int day, int epoch) {

        byte[] prefix = BytesUtils.byteConcatination(BytesUtils.numToBytes(day, 4), BytesUtils.numToBytes(epoch, 1));
        byte[] zeroByteEleven = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] zeroByteTen = new byte[]    {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        byte[] epochEnc = Crypto.AES(epochKey, BytesUtils.byteConcatination(prefix, zeroByteEleven));

        byte[] message = BytesUtils.byteConcatination(prefix, zeroByteTen);
        byte[] epochMac = Crypto.AES(epochEnc, message);

        return new Pair<byte[], byte[]>(epochEnc, epochMac);
    }

    public static byte[] getKeyForDayI(byte[] dayMasterKey) {

        byte[] dayKey = Crypto.HMAC(dayMasterKey, Constants.ENCODED_STRINGS.get("ddaykey"));
        return Arrays.copyOf(dayKey, Constants.KEY_LEN);
    }
}
