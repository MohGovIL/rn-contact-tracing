//
//  Constants.swift
//
//  Created by Yonatan Rimon on 12/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

struct Constants {
    // All time constants are in seconds
    static let T_DAY = 24*60*60
    // 1 hour
    static let T_EPOCH = 60*60
    static let T_UNIT = 5*60
    static let UNITS_IN_EPOCH = 60*60 / (5*60)
    static let JITTER_THRESHOLD = 10*60
    static let EPOCHS_IN_DAY = 24*60*60 / (60*60)
    static let T_WINDOW = 5*60
    static let MAX_CONTACTS_IN_WINDOW = 1000
    
}
