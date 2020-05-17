//
//  Keys.swift
//
//  Created by hagai rothschild on 10/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation
import CommonCrypto

let const_KEY_LEN:Int = 16;
let const_MESSAGE_LEN:Int = 16;
let const_HMAC_KEY_LEN:Int = 64;


//# Represents a set of keys an infected user sends to the server
//# Keys are stored as tupples: (day, epoch, PreK_epoch)


// User key of infected users. should be sent to the server if an infected user agrees.
//
// :param user_id:                     user id.
// :param key_id:                      Identification key
// :param pre_epochs:                  cryptographic keys for users to check for contact.
// :param key_master_verification:     key for validating user.
class UserKey
{
    var ID:[UInt8];
    var K_ID:[UInt8];
    var preEpoch:[(Int, Int, [UInt8])];
    var K_masterVER:[UInt8];
    
    init(user_id: [UInt8], key_id: [UInt8], pre_epochs: [(Int, Int, [UInt8])], key_master_verification: [UInt8])
    {
                
        self.ID = user_id;
        self.K_ID = key_id;
        self.preEpoch = pre_epochs;
        self.K_masterVER = key_master_verification;
    }
}


// Day key to derive epoch keys from.
//
// :param i:                           The corresponding day
// :param master_key:                  The current day master key. Use get_next_master_key.
// :param key_master_com:              The master commitment key.
// :param key_master_verification:     Master verification key for proofing id.
class DayKey
{
    var day:[UInt8];
    var verification:[UInt8];
    var commit:[UInt8];
    
    init(i: Int, master_key: [UInt8], key_master_com: [UInt8], key_master_verification: [UInt8])
    {

        self.day = Crypto.hmac_sha256_firstItems(key: master_key, data: STRINGS["ddaykey"]!, numberOfItems: const_KEY_LEN);
        self.verification = Crypto.hmac_sha256_firstItems(key:key_master_verification,data: BytesUtils.numToBytes(num: i, numBytes: 4) + STRINGS["dverif"]!, numberOfItems: const_KEY_LEN);
        self.commit = DerivationUtils.get_key_commit_i(key_master_com: key_master_com, day: BytesUtils.numToBytes(num: i, numBytes: 4))
    }
}

// Key for a specific epoch.
//
// :param i:           epoch day.
// :param j:           epoch index
// :param k_day:       day key.
//
class EpochKey : Codable
{
    var preKey:[UInt8];
    var epoch:[UInt8];
    var epochENC:[UInt8];
    var epochMAC:[UInt8];
    var epochVER:[UInt8];
    
    init(i: Int, j: Int, k_day: DayKey)
    {

        let time_prefix = BytesUtils.numToBytes(num: i, numBytes: 4) + BytesUtils.numToBytes(num: j, numBytes: 1)
        self.preKey = Crypto.encrypt(key: k_day.day, plain: time_prefix + Array(repeating: 0, count: 11))
        
        self.epoch = DerivationUtils.get_key_epoch(pre_key: self.preKey, commit: k_day.commit, day: BytesUtils.numToBytes(num: i, numBytes: 4), epoch: BytesUtils.numToBytes(num: j, numBytes: 1))
        self.epochENC = Crypto.encrypt(key: self.epoch, plain: time_prefix + Array(repeating: 0, count: 11))
        self.epochMAC = Crypto.encrypt(key: self.epoch, plain: time_prefix
        + [1] + Array(repeating: 0, count: 10))
        self.epochVER = Crypto.encrypt(key: k_day.verification, plain: time_prefix + Array(repeating: 0, count: 11))
    }
}
