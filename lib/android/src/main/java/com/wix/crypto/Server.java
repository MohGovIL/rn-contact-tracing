package com.wix.crypto;

import com.wix.crypto.custom.Pair;
import com.wix.crypto.custom.Triplet;
import com.wix.crypto.key.UserKey;
import com.wix.crypto.utilities.BytesUtils;
import com.wix.crypto.utilities.DerivationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hagai on 13/05/2020.
 */
public class Server {

    public static int USER_RAND_LEN = 4;
    private Map<Integer, Map<Integer, ArrayList<Pair<byte[],byte[]>>>> epochs;


    public Server()
    {
        this.epochs = new HashMap<>();
    }

    public void receive_user_commit()
    {

    }

    public void receive_user_key(UserKey user_key)
    {
        byte[] key_com_master = DerivationUtils.getMasterKeyCommitment(user_key.getKeyId(), user_key.getUserId());
        //TODO:: delete user_key.K_ID, user_key.ID
        //# From this point, we will no longer need K_ID nor ID. These values should be deleted.
        // del user_key.K_ID, user_key.ID

        byte[] key_master_verification = user_key.getKeyMasterVerification();
        byte[] key_com_daily = new byte[]{};

        for (int i = 0; i < user_key.getPreEpoch().size(); i++)
        {
            Triplet<Integer, Integer, byte[]> currentEpoch = user_key.getPreEpoch().get(i);
            if (!this.epochs.containsKey(currentEpoch.getFirst()))
                this.epochs.put(currentEpoch.getFirst(), new HashMap<Integer, ArrayList<Pair<byte[], byte[]>>>()) ;
            if (!this.epochs.get(currentEpoch.getFirst()).containsKey(currentEpoch.getSecond()))
                this.epochs.get(currentEpoch.getFirst()).put(currentEpoch.getSecond(),new ArrayList<Pair<byte[], byte[]>>());

            byte[] daily_commit_key = DerivationUtils.getKeyCommitmentForDayI(key_com_master, BytesUtils.numToBytes(currentEpoch.getFirst(), 4));


            byte[] epoch_key = DerivationUtils.getKeyEpoch(currentEpoch.getThird(), daily_commit_key, BytesUtils.numToBytes(currentEpoch.getFirst(), 4), BytesUtils.numToBytes(currentEpoch.getSecond(), 1));
            byte[] daily_verification_key = DerivationUtils.getKeyVerificationForDayI(key_master_verification, currentEpoch.getFirst());
            byte[] padding = new byte[]{0,0,0,0,0,0,0,0,0,0,0};
            byte[] epoch_ver = Crypto.AES(daily_verification_key, BytesUtils.byteConcatenation(BytesUtils.numToBytes(currentEpoch.getFirst(), 4), BytesUtils.byteConcatenation(BytesUtils.numToBytes(currentEpoch.getSecond(), 1), padding)));
            this.epochs.get(currentEpoch.getFirst()).get(currentEpoch.getSecond()).add(new Pair<>(epoch_key,epoch_ver));
            Collections.shuffle(this.epochs.get(currentEpoch.getFirst()).get(currentEpoch.getSecond()));

            // randomize the order
//            this.epochs.get(currentEpoch.getFirst()).put(currentEpoch.getSecond(),this.epochs.get(currentEpoch.getFirst()).get(currentEpoch.getSecond()));
        }
    }

    public Map<Integer, Map<Integer, ArrayList<byte[]>>> send_keys()
    {
        //#TODO some kind of delete - old - keys mechanism
        Map<Integer, Map<Integer, ArrayList<byte[]>>> epochsNew = new HashMap<>();
        for (Integer day : this.epochs.keySet())
        {
            epochsNew.put(day, new HashMap<Integer, ArrayList< byte[]>>());
            for (Integer epoch : this.epochs.get(day).keySet())
            {
                epochsNew.get(day).put(epoch, new ArrayList< byte[]>());
                for (Pair<byte[],byte[]> pair: this.epochs.get(day).get(epoch))
                {
                    epochsNew.get(day).get(epoch).add(pair.getFirst());
                }
            }
        }

        return epochsNew;
    }

    public boolean verify_contact(int day, int epoch, byte[] proof)
    {
        if(this.epochs.get(day) != null && this.epochs.get(day).get(epoch) != null)
        {
            ArrayList<Pair<byte[], byte[]>> pairs = this.epochs.get(day).get(epoch);
            for (int i = 0; i < pairs.size(); i++) {
                Pair<byte[], byte[]> currPair = pairs.get(i);
                byte[] part = new byte[USER_RAND_LEN];
                System.arraycopy(currPair.getSecond(), 0, part, 0, USER_RAND_LEN);

                if (Arrays.equals(part, proof)) {
                    return true;
                }
            }
        }
        return false;
    }
}
