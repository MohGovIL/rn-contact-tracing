package moh.gov.il.crypto.utilities;

import moh.gov.il.crypto.Constants;
import moh.gov.il.crypto.Crypto;
import moh.gov.il.crypto.custom.Pair;

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

        byte[] message = BytesUtils.byteConcatenation(userId, Constants.ENCODED_STRINGS.get("idCom"));
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

        byte[] messagePart1 = BytesUtils.byteConcatenation(commitKey, day);
        byte[] messagePart2 = BytesUtils.byteConcatenation(epoch, Constants.ENCODED_STRINGS.get("depoch"));
        byte[] message = BytesUtils.byteConcatenation(messagePart1, messagePart2);

        byte[] epochKey = Crypto.HMAC(preKey, message);

        return Arrays.copyOf(epochKey, Constants.KEY_LEN);
    }

    public static byte[] getKeyCommitmentForDayI(byte[] keyMasterCommitment, byte[] day) {
        assert day.length == 4;

        byte[] zeroBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] message = BytesUtils.byteConcatenation(day, zeroBytes);

        return Crypto.AES(keyMasterCommitment, message);
    }

    public static byte[] getKeyVerificationForDayI(byte[] keyMasterVerification, int dayI) {

        byte[] day = BytesUtils.numToBytes(dayI, 4);
        byte[] message = BytesUtils.byteConcatenation(day, Constants.ENCODED_STRINGS.get("dverif"));

        byte[] result = Crypto.HMAC(keyMasterVerification, message);

        return Arrays.copyOf(result, Constants.KEY_LEN);
    }


    public static Pair<byte[],byte[]> getEpochKeys(byte[] epochKey, int day, int epoch) {

        byte[] prefix = BytesUtils.byteConcatenation(BytesUtils.numToBytes(day, 4), BytesUtils.numToBytes(epoch, 1));
        byte[] zeroByteEleven = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] zeroByteTen = new byte[]    {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        byte[] epochEnc = Crypto.AES(epochKey, BytesUtils.byteConcatenation(prefix, zeroByteEleven));

        byte[] message = BytesUtils.byteConcatenation(prefix, zeroByteTen);
        byte[] epochMac = Crypto.AES(epochKey, message);

        return new Pair<>(epochEnc, epochMac);
    }

    public static byte[] getKeyForDayI(byte[] dayMasterKey) {

        byte[] dayKey = Crypto.HMAC(dayMasterKey, Constants.ENCODED_STRINGS.get("ddaykey"));
        return Arrays.copyOf(dayKey, Constants.KEY_LEN);
    }
}
