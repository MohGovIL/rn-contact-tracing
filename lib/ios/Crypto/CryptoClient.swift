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
        // TODO: change after tests
        return String(("GATT_" + UIDevice.current.name).prefix(16))
//        return CryptoManager.shared.getStringEphemeral()
    }
    
    @objc(saveMyUserToDisk)
    static public func saveMyUserToDisk() {
        CryptoManager.shared.saveMyUserToDisk()
    }
    
    @objc(printDecodedKey:)
    static public func printDecodedKey(_ k: String) {
        let a = k.asciiToUInt8Bytes()
        print("decoded publicKey received: \(a)")
    }
    
    @objc(findMatch::)
    static public func findMatch(startDay: Int, infectedArray: [[[String]]]) -> String  {
        return CryptoManager.shared.findMatch(startDay: startDay, infectedArray: infectedArray)
    }
    
    @objc(fetchInfectionDataByConsent)
    static public func fetchInfectionDataByConsent() -> String {
        
        return CryptoManager.shared.fetchInfectionDataByConsent()
    }

}
