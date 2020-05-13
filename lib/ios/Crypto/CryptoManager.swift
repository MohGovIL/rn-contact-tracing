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
//        super.init()
        mySelf = fetchUser()
    }
    
    private func writeUserToDefaults(_ user: User) {
//        let encoder = JSONEncoder()
//        if let encoded = try? encoder.encode(mySelf) {
//            let defaults = UserDefaults.standard
//            defaults.set(encoded, forKey: "SavedModel")
//            defaults.synchronize()
//        }

        let jsonData = try! JSONEncoder().encode(user)
        let userDefaults = UserDefaults.standard
//        let encodedData: Data = NSKeyedArchiver.archivedData(withRootObject: mySelf)
        userDefaults.set(jsonData, forKey: "mySelf")
        userDefaults.synchronize()
    }
    
    func getStringEphemeral() -> String {
        let timeS = Int(Date().timeIntervalSince1970)
        let geoHash:[UInt8] = Array(repeating: 1, count: 5)
        let ephemeral = mySelf.generate_ephemeral_id(time: timeS, geo_hash: geoHash)
        var str = ""
        for byte in ephemeral {
            let u = UnicodeScalar(byte)
            // Convert UnicodeScalar to a Character.
            let char = Character(u)

            // Write results.
//            print(char)

            str.append(char)
        }
        
        let stringArray = Array(str)
        var res:[UInt16] = []
        for i in 0..<stringArray.count {
            res.append(contentsOf: stringArray[i].utf16)
        }
        
        return str
        
//        let ephemeralString = String(bytes: ephemeral, encoding: .ascii)
//        let b = ephemeral.reduce("", { $0 + String(format: "%c", $1)})
//        return ephemeralString!
    }
    
    
    @objc(fetchUser)
    func fetchUser() -> User! {
        if let decoded  = UserDefaults.standard.data(forKey: "mySelf") {
            let user = try! JSONDecoder().decode(User.self, from: decoded)
//            let user = NSKeyedUnarchiver.unarchiveObject(with: decoded) as! User
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
    
    
    
    func fetchInfectionDataByConsent() -> [Int: [Int : [[UInt8]]]] {
        let server = Server()
        
        let server_keys_a = mySelf!.get_keys_for_server()
        server.receive_user_key(user_key: server_keys_a)
        let server_msg = server.send_keys()
        return server_msg;
    }
}


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
}


