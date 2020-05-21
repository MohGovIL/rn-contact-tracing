//
//  utilities.swift
//
//  Created by hagai rothschild on 10/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

class Match : Codable
{
    var contact: Contact;
    var infected_geohash: [UInt8];
    var proof: [UInt8];
    var infected_time: Int;
    var matchEpoc: [UInt8];
    init(contact: Contact, ephid_geohash: [UInt8], ephid_user_rand: [UInt8], other_time: Time, other_unit: Int)
    {
        //        :param contact:             Contact with the other user.
        //        :param ephid_geohash:       Other user ephemeral id.
        //        :param ephid_user_rand:     Other user proof.
        //        :param other_time:          Other user epoch time of contact.
        //        :param other_unit:          Other user time unit.
   
        self.contact = contact
        self.infected_geohash = ephid_geohash
        self.proof = ephid_user_rand
        // Up to T_UNIT
        self.infected_time = other_time.day * Constants.T_DAY + other_time.epoch * Constants.T_EPOCH + other_unit * Constants.T_UNIT
        self.matchEpoc = [UInt8]()
    }
}


