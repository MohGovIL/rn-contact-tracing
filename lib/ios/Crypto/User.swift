//
//  User.swift
//
//  Created by hagai rothschild on 10/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

let const_USER_RAND_LEN = 4
let const_GEOHASH_LEN = 5

@objc(User)
public class User : NSObject, Codable {
    var user_id:[UInt8] = []
    var K_id:[UInt8] = []
    var K_master_com:[UInt8] = []
    var K_master_ver:[UInt8] = []
    var epoch_keys:[Time:EpochKey] = [:]
    var curr_day: Int = 0
    var curr_day_master_key:[UInt8] = []
    
    override init(){
        self.user_id = []
        self.K_id = []
        self.K_master_com = []
        self.K_master_ver = []
        self.epoch_keys = [:]
        self.curr_day = 0
        self.curr_day_master_key = []
    }
    
    init(user_id: [UInt8], master_key: [UInt8], init_time: Int) {
        super.init()
        self.user_id = user_id
        self.K_id = Crypto.hmac_sha256_firstItems(key: master_key, data: STRINGS["id"]!, numberOfItems: const_KEY_LEN)
        self.K_master_com = DerivationUtils.get_key_master_com(key_id: self.K_id, user_id: self.user_id)
        self.K_master_ver = Crypto.hmac_sha256_firstItems(key: master_key, data: STRINGS["verifkey"]!, numberOfItems: const_KEY_LEN)
        self.epoch_keys = [:]
        self.curr_day = Time(init_time).day
        self.curr_day_master_key = DerivationUtils.get_next_day_master_key(prev_master_key: master_key, install_day: true)
        self._get_epoch_keys(target_day: self.curr_day)
    }
    
    // Update the key database to store key's of specific time intreval.
    // To protect the user's privacy, all information (including contact information)
    // older than past_time will be deleted.
    //
    // :param past_time:   start time to store keys(should be a time from the past).
    //                     All stored keys before past_time will be deleted.
    // :param future_time: end time to store keys(should be a time from the future).
    func update_key_databases(past_time: Int, future_time: Int) {
        let past = Time(past_time);
        let future = Time(future_time);

        self._get_epoch_keys(target_day: future.day)
        self.delete_history(dtime: past.time)
    }
    
    // Generate the ephemeral id of a specific time. User should have the right epoch keys.
    //
    // :param time:        current time.
    // :param geo_hash:    current location.
    // :return:            The ephemeral id.
    // ** Raises an error if epoch keys are not present.
    func generate_ephemeral_id(time: Int, geo_hash: [UInt8]) -> [UInt8] {
    
        if geo_hash.count != const_GEOHASH_LEN {
            return [UInt8]()
        }
        let t = Time(time);
        // check that time key exists...
        if self.epoch_keys[t] == nil {
            let timestamp = Int(Date().timeIntervalSince1970)
            update_key_databases(past_time: timestamp - 14 * 24 * 3600, future_time: timestamp)
            if self.epoch_keys[t] == nil {
                return [UInt8]()
            }
        }
        
        let time_unit_s = t.get_units()
        let epoch_key = self.epoch_keys[t]!
        
        let mask = Crypto.encrypt(key: epoch_key.epochENC, plain: BytesUtils.numToBytes(num: time_unit_s, numBytes: const_MESSAGE_LEN))
        let user_rand = Array(epoch_key.epochVER[0..<const_USER_RAND_LEN])
        
        let plain = [0x00, 0x00, 0x00] + geo_hash + user_rand + [0x00, 0x00, 0x00, 0x00]
        let c_ijs = BytesUtils.xor(plain, mask)
        return Array(c_ijs[0..<12]) + Array(Crypto.encrypt(key: epoch_key.epochMAC, plain: c_ijs)[0..<4])
    }
    
//    Check for matching with the infected user.
//    :param infected_key_database:
//    :return: List of matches with the infected user.
    func find_crypto_matches(infected_key_database: [ Int : [Int:[ [UInt8] ] ] ]) -> [Match] {
        var matches:[Match] = []
        
        // Make sure the contacts are sorted so as to make the sliding window work properly
        // domain is a time up to units (actually a string "day-epoch-unit")
        // and its range is a list of (mask, epochMAC)
        var unit_keys:[String:[([UInt8],[UInt8],[UInt8])]] = [:]
        var earliest_time = 0
        if let fetchedObjects = DBClient.getContacts().fetchedObjects {
            for contact in fetchedObjects {
                var time = contact.timestamp - Constants.JITTER_THRESHOLD
                
                // Remove all entries unit_keys[t] for t < time (will save memory usage)
                // For it to work we need self.contact to be ordered by contact.time
                if earliest_time != 0 {
                    while earliest_time < time {
                        let t_dict_key = Time(earliest_time).str_with_units()
                        unit_keys.removeValue(forKey: t_dict_key)
                        earliest_time += Constants.T_UNIT
                    }
                } else {
                    earliest_time = time
                }
                if(contact.timestamp == 0)
                {
                    print("--------------------------")
                }
                
                while time <= contact.timestamp + Constants.JITTER_THRESHOLD {
                    let t = Time(time)
                    let t_key = t.str_with_units()
                    let unit = t.get_units()
                    time += Constants.T_UNIT
                    
                    if unit_keys[t_key] == nil {
                        unit_keys[t_key] = [([UInt8], [UInt8], [UInt8])]()
                        if infected_key_database[t.day] != nil && (infected_key_database[t.day]![t.epoch] != nil) {
                            for epoch_key in infected_key_database[t.day]![t.epoch]! {
                                let epoch_enc = DerivationUtils.get_epoch_keys(epoch_key: epoch_key, day: t.day, epoch: t.epoch).0
                                let epoch_mac = DerivationUtils.get_epoch_keys(epoch_key: epoch_key, day: t.day, epoch: t.epoch).1
                                let mask = Crypto.encrypt(key: epoch_enc, plain: BytesUtils.numToBytes(num: unit, numBytes: const_MESSAGE_LEN))
                                let newTuple = (mask, epoch_mac, epoch_key)
                                unit_keys[t_key]!.append(newTuple)
                            }
                        }
                    }
                    
                    for (mask, epoch_mac, epoch_key) in unit_keys[t_key]! {
                        let match = self._is_match(mask: mask, epoch_mac: epoch_mac, contact: contact)
                        if match.0 == true {
                            let matchFound = Match(contact: contact, ephid_geohash: match.1, ephid_user_rand: match.2, other_time: t, other_unit: unit)
                            matchFound.matchEpoc = epoch_key
                            matches.append(matchFound)
                        }
                    }
                }
            }
        }
        return matches
    }
    
    // Delete my keys in a time period.
    // :param start_time:              start time of period to delete.
    // :param end_time:                end time of period to delete.
    // :note:                          only deleting key and not contacts.
    func  delete_my_keys(start_time: Int, end_time: Int) {
        let start = Time(start_time)
        let end = Time(end_time)
        
        //  TODO [RA]: what if end_time is in the future?
        var epoch_keys_to_delete = [Time]()
        
        for time in self.epoch_keys.keys {
            if start <= time && time <= end {
                epoch_keys_to_delete.append(time)
            }
        }
        for key in epoch_keys_to_delete {
            self.epoch_keys.removeValue(forKey: key)
        }
    }
    
    // Get a willing infected user keys.
    // The keys will be sent to all user by the server.
    // :return: Keys to be sent to the server.
    func  get_keys_for_server() -> UserKey {
        /*
         # TODO I assume all currently existing keys are relevant.
         # Perhaps a 'checkup' is needed, i.e. remove old keys and so.
         # TODO similarly, one needs to make sure all non-deleted epoch keys
         #  are in self.epoch_keys (i.e. they were, at some point, derived from the daily key)
         # TODO current version only sends epochs, i.e. does not try to reduce bandwidth by sending daily keys.
         */
        var epochs:[(Int, Int, [UInt8])] = []
        for T in self.epoch_keys.keys {
            let epochTuple = (T.day, T.epoch, self.epoch_keys[T]!.preKey)
            epochs.append(epochTuple)
        }
        let keys = UserKey(user_id: self.user_id, key_id: self.K_id, pre_epochs: epochs, key_master_verification: self.K_master_ver)
        return keys
    }
    
    // Store an ephemeral id from BLE received in the wild.
    // :param other_ephemeral_id:  other user ephemeral id.
    // :param rssi:                ?
    // :param time:                current time.
    // :param own_location:        current location
    func  store_contact(other_ephemeral_id: [UInt8], rssi: Int, time: Int, own_location: [UInt8],lat: Double, lon: Double) -> Bool {
        let contactsCount = DBClient.getContacts().fetchedObjects?.count ?? 0
        if contactsCount > 0 && time < (DBClient.getContacts().fetchedObjects!.last!).timestamp - Constants.JITTER_THRESHOLD {
            // We expect contacts to come in chronological order
            // Up to jitter
            return false
        }
        
        DBContactManager.shared.addNewContact(ephemeral_id: other_ephemeral_id, rssi: rssi, time: time, location: own_location, id: contactsCount+1,lat: lat, lon: lon)

        return true
    }
    
    // deletes a contact from local contact DB.
    // :param contact: Contact to delete.
    func  delete_contact(contact: Contact) {
        DBClient.deleteContact(contact)
    }
    
    // Delete all history before a specific time.
    // Should be used also to delete days that are more then 14 days in the past.
    // :param dtime:   time to delete history from
    // :note:          deleting all keys and contacts.
    func  delete_history(dtime: Int) {
        // Deletes all local information for time < dtime.
        // This include all keys and contacts.
        let t = Time(dtime)
        
        self.epoch_keys = self.epoch_keys.filter { $0.key >= t }
        CryptoClient.saveMyUserToDisk()
        DBClient.deleteContactsHistory(dtime: dtime)
    }
    
    // Makes sure the user has all epoch keys corresponding to a given day
    // :param target_day:     day to update.
    func _get_epoch_keys(target_day: Int) {
        // If all keys are present, nothing to be done.
        var allKeysArePresent = true
        for epoch in [Int](0..<Constants.EPOCHS_IN_DAY) {
            if !self.epoch_keys.keys.contains(Time(target_day,epoch)) {
                allKeysArePresent = false
                break
            }
        }
        if allKeysArePresent == true {
            return
        }
        
        // Else, we need to generate keys.
        if self.curr_day > target_day { // Cannot retrieve keys from the past
            print("Cannot retrieve keys from the past")
            return
        }
        
        while self.curr_day <= target_day {
            let curr_day_key = DayKey(i: self.curr_day, master_key: self.curr_day_master_key, key_master_com: self.K_master_com, key_master_verification: self.K_master_ver)
            for epoch in [Int](0..<Constants.EPOCHS_IN_DAY) {
                self.epoch_keys[Time(self.curr_day, epoch)] = EpochKey(i: self.curr_day, j: epoch, k_day: curr_day_key)
            }
            self.curr_day += 1
            self.curr_day_master_key = DerivationUtils.get_next_day_master_key(prev_master_key: self.curr_day_master_key, install_day: false)
        }
    }
    
    func _is_match(mask: [UInt8], epoch_mac: [UInt8], contact: Contact) -> (Bool, [UInt8], [UInt8]) {
        if mask.count != 16 || epoch_mac.count != 16 || contact.ephemeral_id.count != 16 {
            return (false, [0x00], [0x00])
        }
        let ephid = [UInt8](contact.ephemeral_id)
        let plain = BytesUtils.xor(mask, ephid)
        let zeros = Array(plain[0..<3])
        let ephid_geohash = Array(plain[3..<3+const_GEOHASH_LEN])
        let ephid_user_rand = Array(plain[3+const_GEOHASH_LEN..<3 + const_GEOHASH_LEN + const_USER_RAND_LEN])
        
        // First three bytes of plaintext are zero
        for x in zeros {
            if x != 0 {
                return (false, [0x00], [0x00])
            }
        }
        let x = Array(ephid[0..<ephid.count-4]) + Array(mask[mask.count-4..<mask.count])
        let y = Array(ephid[ephid.count-4..<ephid.count])
        if y == Array(Crypto.encrypt(key: epoch_mac, plain: x)[0..<4]) {
            return (true, ephid_geohash, ephid_user_rand)
        }
        return (false, [0x00], [0x00])
    }
}
