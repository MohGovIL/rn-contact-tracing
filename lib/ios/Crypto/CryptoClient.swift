//
//  CryptoClient.swift
//  CryptoSwift
//
//  Created by Yonatan Rimon on 13/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

@objc(CryptoClient)
public class CryptoClient : NSObject {
    
    @objc(getMyUser)
    static public func getMyUser() -> User! {
        return CryptoManager.shared.mySelf
    }
    
    @objc(getEphemeralId)
    static public func getEphemeralId() -> String {
        return CryptoManager.shared.getStringEphemeral()
    }
    
    
}
