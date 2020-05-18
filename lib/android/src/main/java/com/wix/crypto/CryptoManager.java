package com.wix.crypto;

import android.content.Context;
import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hagai on 12/05/2020.
 */
public class CryptoManager
{

    private static CryptoManager sManagerInstance;
    private Context mCtx;

    private CryptoManager(Context ctx)
    {
        mCtx = ctx;
        fetchUserOrCreate();
    }

    public User mySelf;

    public static CryptoManager getInstance(Context ctx) 
    {
        if (sManagerInstance == null)
        {
            sManagerInstance = new CryptoManager(ctx.getApplicationContext());
        }
        return sManagerInstance;
    }

    public void fetchUserOrCreate()
    {
        boolean userExists = false;

        if((mySelf = getUserFromDb()) != null)
        {
            // user exists
            // assure all runtime initiation phase
        }
        else
        {
            createNewUser();
        }
    }

    public void createNewUser()
    {
        SecureRandom random = new SecureRandom();
        byte[] master_key = new byte[16];
        random.nextBytes(master_key);
        byte[] user_id = new byte[16];
        random.nextBytes(master_key);
        mySelf = new User(user_id, master_key, (int)(System.currentTimeMillis() / 1000), mCtx);
    }

    private User getUserFromDb()
    {
        return User.deserialize(mCtx);
    }

    public Map<Integer, Map<Integer, ArrayList<byte[]>>> fetchInfectionDataByConsent()
    {
        Server server = new Server();

        server.receive_user_key(mySelf.getKeysForServer());
        return server.send_keys();
    }
}
