//
//  Server.swift
//
//  Created by Yonatan Rimon on 11/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

let USER_RAND_LEN = 4

//# note that no identification data is stored on the server.
//# The keys are identified only using the MoH generated TestCode.
class Server {
    var epochs:[Int:[Int:[([UInt8],[UInt8])]]]
    
    init () {
        // self.epochs[day][epoch] = [randomized list of (epoch key, verification key)]
        self.epochs = [:]
    }
    
    func  receive_user_commit(user_commit_id: Any, user_key_id: Any, test_code: Any) {
        
    }
    
    // Receive a key from an infected user key who agreed to send his keys.
    // :param user_key:
    func receive_user_key(user_key: UserKey) {
        // TODO get a test code and verify it against ID
        let key_com_master = DerivationUtils.get_key_master_com(key_id: user_key.K_ID, user_id: user_key.ID)
        // From this point, we will no longer need K_ID nor ID. These values should be deleted.
        user_key.K_ID = [UInt8]()
        user_key.ID = [UInt8]()

        let key_master_verification = user_key.K_masterVER
//        var key_com_daily:[Int:[UInt8]] = [:]
        
        for (day, epoch, k_pre_epoch) in user_key.preEpoch {
            if !self.epochs.keys.contains(day) {
                self.epochs[day] = [:]
            }
            if !self.epochs[day]!.keys.contains(epoch) {
                self.epochs[day]![epoch] = []
            }
            let daily_commit_key = DerivationUtils.get_key_commit_i(key_master_com: key_com_master, day: BytesUtils.numToBytes(num: day, numBytes: 4))
            let epoch_key = DerivationUtils.get_key_epoch(pre_key: k_pre_epoch, commit: daily_commit_key, day: BytesUtils.numToBytes(num: day, numBytes: 4), epoch: BytesUtils.numToBytes(num: epoch, numBytes: 1))
            let daily_verification_key = DerivationUtils.get_key_i_verification(key_master_verification: key_master_verification, day: day)
            let plainArray = BytesUtils.numToBytes(num: day, numBytes: 4) + BytesUtils.numToBytes(num: epoch, numBytes: 1) + [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]
            let epoch_ver = Crypto.encrypt(key: daily_verification_key, plain: plainArray)
            
            self.epochs[day]![epoch]?.append((epoch_key, epoch_ver))
            
            // TODO: fix randomize ???
            // randomize the order
            self.epochs[day]![epoch]?.shuffle()// = self.epochs[day]![epoch]?.sorted(by: {$0<$1})
            //            self.epochs[day][epoch] = sorted(self.epochs[day][epoch], key=lambda x: x[0])
        }
    }
    
    func send_keys() -> [Int:[Int:[[UInt8]]]] {
        // TODO some kind of delete-old-keys mechanism
        var epochs:[Int:[Int:[[UInt8]]]] = [:]
        for day in self.epochs.keys {
            epochs[day] = [:]
            for epoch in self.epochs[day]!.keys {
                epochs[day]![epoch] = self.epochs[day]![epoch]!.map { $0.0 }
//                for x in self.epochs[day]![epoch]! {
//                    epochs[day]![epoch]?.append(x.0)
//                }
            }
        }
        return epochs
    }
    
    // :note: if proof is correct, self.epochs[day][epoch] should contain a tuple (epoch_key, epoch_ver)
    //        such that epoch_ver's first four bytes are proof
    // :param day:
    // :param epoch:
    // :param proof:
    func  verify_contact(day: Int, epoch: Int, proof: [UInt8]) -> Bool {
        if self.epochs[day] != nil && self.epochs[day]![epoch] != nil {            
            for x in self.epochs[day]![epoch]! {
                if Array(x.1[0..<USER_RAND_LEN]) == proof {
                    return true
                }
            }
        }
        return false
    }
}
