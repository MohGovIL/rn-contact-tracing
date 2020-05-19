package com.wix.crypto;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hagai on 11/05/2020.
 */
public class Constants {

    public static final Map<String, byte[]> ENCODED_STRINGS = new HashMap<String, byte[]>() {

        {
            put("id", "IdentityKey".getBytes(StandardCharsets.US_ASCII));
            put("idCom", "IdentityCommitment".getBytes(StandardCharsets.US_ASCII));
            put("master0", "DeriveMasterFirstKey".getBytes(StandardCharsets.US_ASCII));
            put("master", "DeriveMasterKey".getBytes(StandardCharsets.US_ASCII));
            put("ddaykey", "DeriveDayKey".getBytes(StandardCharsets.US_ASCII));
            put("dverif", "DeriveVerificationKey".getBytes(StandardCharsets.US_ASCII));
            put("depoch", "DeriveEpoch".getBytes(StandardCharsets.US_ASCII));
            put("verifkey", "VerificationKey".getBytes(StandardCharsets.US_ASCII));
        }

    };

    public static final int KEY_LEN = 16;
    public static final int MESSAGE_LEN = 16;
    public static final int HMAC_KEY_LEN = 64;
    public static final int GEOHASH_LEN = 5;
    public static final int USER_RAND_LEN = 4;
    public static final int None = -1;
    public static final int NUM_OF_DAYS = 14; // Zero based index
    public static final int NUM_OF_EPOCHS = 24;
    public static final int SECONDS_IN_DAY = 24 * 3600;
}
