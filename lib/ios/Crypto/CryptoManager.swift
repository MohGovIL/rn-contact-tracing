//
//  CryptoManager.swift
//
//  Created by Yonatan Rimon on 13/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

public class CryptoManager {
    
    static let shared = CryptoManager()
    var mySelf:User!
    
    
    // MARK:- LifeCycle
    private init(){
        mySelf = fetchUser()
    }
    
    // MARK:- Private funcs
    fileprivate func writeUserToDefaults(_ user: User) {
        let jsonData = try! JSONEncoder().encode(user)
        let userDefaults = UserDefaults.standard
        userDefaults.set(jsonData, forKey: "mySelf")
        userDefaults.synchronize()
    }
    
    fileprivate func fetchUser() -> User! {
        if let decoded  = UserDefaults.standard.data(forKey: "mySelf") {
            let user = try! JSONDecoder().decode(User.self, from: decoded)
            return user
        } else {
            let masterKey = [UInt8](Data(randomOfLength: 16))
            let userKey = [UInt8](Data(randomOfLength: 16))
            let init_time = Int(Date().timeIntervalSince1970)
            print("masterKey: \(masterKey)")
            print("userKey: \(userKey)")
            print("init_time: \(init_time)")
            let user = User(user_id: userKey, master_key: masterKey, init_time: init_time)
            writeUserToDefaults(user)
            return user
        }
    }
    
    // MARK:- User funcs
    func getStringEphemeral() -> String {
        let timeS = Int(Date().timeIntervalSince1970)
        let geoHash:[UInt8] = Array(repeating: 1, count: 5)
        let ephemeralUInt8 = mySelf.generate_ephemeral_id(time: timeS, geo_hash: geoHash)
        
        // Encode to string
        var ephemeralString = ""
        for byte in ephemeralUInt8 {
            let u = UnicodeScalar(byte)
            // Convert UnicodeScalar to a Character.
            let char = Character(u)
            ephemeralString.append(char)
        }
        
        // decode the string key back to [UInt8], use:
//        let decodedEphemeral = ephemeralString.asciiToUInt8Bytes()
        
//        print("Ephemeral: \(ephemeralUInt8)")
//        print("String:    \(ephemeralString)")
//        print("Decoded:   \(decodedEphemeral)")
//        print("bytes r ==:\(ephemeralUInt8 == decodedEphemeral)")
        
        return ephemeralString
    }
    
    func fetchInfectionDataByConsent() -> [Int: [Int : [[UInt8]]]] {
        let server = Server()
        
        let server_keys_a = mySelf!.get_keys_for_server()
        server.receive_user_key(user_key: server_keys_a)
        let server_msg = server.send_keys()
        return server_msg;
    }
}

// MARK:- Extentions
extension Data {
    init(randomOfLength length: Int) {
        var bytes = [UInt8](repeating: 0, count: length)
        let status = SecRandomCopyBytes(kSecRandomDefault, length, &bytes)
        if status == errSecSuccess {
            self.init(bytes)
        } else {
            self.init()
        }
    }
}

extension String {
    init?(bytes: UnsafePointer<UInt8>, count: Int) {
        let bp = UnsafeBufferPointer(start: bytes, count: count)
        self.init(bytes: bp, encoding: .utf8)
    }
    
    func asciiToUInt8Bytes() -> [UInt8] {
        // Decode to ByteArray
        let stringArray = Array(self)
        var res:[UInt16] = []
        for i in 0..<stringArray.count {
            res.append(contentsOf: stringArray[i].utf16)
        }
        // convert to [UInt8]
        return res.map{ UInt8($0 & 0x00ff) }
    }
}


