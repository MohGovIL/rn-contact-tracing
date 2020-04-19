//
//  DBPublicKeyManager.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/19/20.
//

import Foundation

class DBPublicKeyManager {
    static let shared = DBPublicKeyManager()
    
    func savePublicKeys(keys: [String]) {
        for key in keys {
            DBManager.shared.save(entity: "PublicKey", name: "publicKey", value:key)
        }
    }
}
