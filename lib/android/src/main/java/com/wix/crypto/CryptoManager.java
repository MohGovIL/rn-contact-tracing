package com.wix.crypto;

import com.wix.crypto.utilities.DerivationUtils;

import java.security.SecureRandom;

/**
 * Created by hagai on 12/05/2020.
 */
public class CryptoManager {

    private static CryptoManager sManagerInstance;
    private byte[] master_key;
    private byte[] key_id;

    private CryptoManager(){}

    public static CryptoManager getInstance() {
        if (sManagerInstance == null)
            sManagerInstance = new CryptoManager();
        return sManagerInstance;
    }

    public void derivInitialKeys()
    {
        //Master Key
        //TODO:: delete after use
        SecureRandom random = new SecureRandom();
        master_key = new byte[16];
        random.nextBytes(master_key);

        //Key ID
        key_id = DerivationUtils.getKeyId(master_key);

        //
    }
}
