//
//  DerivationUtils.swift
//
//  Created by Yonatan Rimon on 10/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

class DerivationUtils
{

    class func get_key_master_com(key_id: [UInt8], user_id: [UInt8]) -> [UInt8]
    {
        return Crypto.hmac_sha256_firstItems(key: key_id, data: user_id + STRINGS["id_com"]!, numberOfItems: const_KEY_LEN);
    }


    class func get_key_epoch(pre_key: [UInt8], commit: [UInt8], day: [UInt8], epoch: [UInt8]) -> [UInt8]
    {
        return Crypto.hmac_sha256_firstItems(key: pre_key, data: commit + day + epoch + STRINGS["depoch"]!, numberOfItems: const_KEY_LEN);
    }


    class func get_key_commit_i(key_master_com: [UInt8], day: [UInt8]) -> [UInt8]
    {
//        return Crypto.encrypt(key_master_com, day + b'\x00' * 12)
        //day must be 4 bytes
        return Crypto.encrypt(key: key_master_com, plain: BytesUtils.pad(array: day, size: const_KEY_LEN));
    }


    class func get_epoch_keys(epoch_key: [UInt8], day: Int, epoch: Int) -> ([UInt8], [UInt8])
    {
        var prefix = BytesUtils.numToBytes(num: day, numBytes: 4) + BytesUtils.numToBytes(num: epoch, numBytes: 1);
        let epoch_enc = Crypto.encrypt(key: epoch_key, plain: BytesUtils.pad(array: prefix, size: const_KEY_LEN));
        //Crypto.encrypt(epoch_key, prefix + b'\x00'*11)
        prefix.append(UInt8(0x01 ));
        let epoch_mac = Crypto.encrypt(key: epoch_key, plain: BytesUtils.pad(array: prefix, size: const_KEY_LEN));
        //var epoch_mac = Crypto.encrypt(epoch_key, prefix + b'\x01' + b'\x00'*10)
        return (epoch_enc, epoch_mac);
    }


    class func get_key_i_verification(key_master_verification: [UInt8], day: Int) -> [UInt8]
    {
        return Crypto.hmac_sha256_firstItems(key: key_master_verification, data: BytesUtils.numToBytes(num: day, numBytes: 4) + STRINGS["dverif"]!, numberOfItems: const_KEY_LEN);
    }


    class func get_next_day_master_key(prev_master_key: [UInt8], install_day: Bool = false) -> [UInt8]
    {
        if (install_day)
        {
            return Crypto.hmac_sha256_firstItems(key: prev_master_key, data: STRINGS["master0"]!, numberOfItems: const_KEY_LEN);
        }
        else
        {
            return Crypto.hmac_sha256_firstItems(key: prev_master_key, data: STRINGS["master"]!, numberOfItems: const_KEY_LEN);
        }
    }

    
}
