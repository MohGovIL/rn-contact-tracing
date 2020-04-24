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
        for (index,key) in keys.enumerated() {
            DBManager.shared.save(entity: "PublicKey", attributes: ["id":String(index),"publicKey":key])
        }
    }
}
