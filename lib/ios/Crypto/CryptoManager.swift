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
    fileprivate func writeUserToDefaults(user: User, key: String) {
        let jsonData = try! JSONEncoder().encode(user)
        let userDefaults = UserDefaults.standard
        userDefaults.set(jsonData, forKey: key)
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
            writeUserToDefaults(user: user, key: "mySelf")
            return user
        }
    }
    
    // MARK:- User funcs
    func saveMyUserToDisk() {
        writeUserToDefaults(user: mySelf,key: "mySelf")
    }
    
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
        return ephemeralString
    }
    
    func fetchInfectionDataByConsent() -> String { // -> [Int: [Int : [[UInt8]]]] {
        let server = Server()
        
        let server_keys_a = mySelf!.get_keys_for_server()
        server.receive_user_key(user_key: server_keys_a)
        let server_msg = server.send_keys()
//        if server_msg.count == 0 {
//            return "{}"
//        }
        if server_msg.count > 0 {
            createNewUserAndSave()
        }
        return parseInfectedDbToJSONString(infetedDB: server_msg);
    }
    
    func findMatch(startDay: Int, infectedArray: [[[String]]]) -> String {
        var infectedEpochs:[Int: [Int : [[UInt8]]]] = [:]
        
        for i in 0..<14 {
            infectedEpochs[i+startDay] = [:]
            for j in 0..<24 {
                let currentEpochsHex = infectedArray[i][j]
                
                if currentEpochsHex.count > 0 {
                    let currentEpochsBinary : [[UInt8]] = currentEpochsHex.map { DBClient.stringToBytes($0)! }
                    
                    infectedEpochs[i+startDay]![j] = currentEpochsBinary
                } else {
                    infectedEpochs[i+startDay]![j] = []
                }
            }
        }
        
        let matches =  Array(mySelf.find_crypto_matches(infected_key_database: infectedEpochs).reversed())
        
//        var matchesResults:[[String:Any]] = []
//        for match in matches {
//            var currentMatch:[String:Any] = [:]
//
//            currentMatch["ephemeral_id"] =  match.contact.ephemeral_id.hex()
//            currentMatch["timestamp"] = match.contact.timestamp
//            currentMatch["geohash"] = match.contact.geohash.hex()
//            currentMatch["rssi"] = match.contact.rssi
//
//            matchesResults.append(currentMatch)
//        }
//        let d = try! JSONSerialization.data(withJSONObject: matchesResults, options: .prettyPrinted)
//
//        let s = String(data: d, encoding: .utf8) ?? ""
//        print(s)
        
        var matchesResultArray : [[String:Any]] = []
        
        if matches.count > 0 {
            for i in 0 ..< matches.count-1
            {
                for j in i+1..<matches.count
                {
                    if matches[i].contact.timestamp - matches[j].contact.timestamp > 1200
                    {
                        break
                    }
                    if 600...1200 ~= matches[i].contact.timestamp - matches[j].contact.timestamp
                    {
                        if (matches[j].matchEpoc == matches[i].matchEpoc)
                        {
                            var matchObject : [String:Any] = [:]
                            matchObject["startContactTimestamp"] = matches[i].contact.timestamp
                            matchObject["endContactTimestamp"] = matches[j].contact.timestamp
                            matchObject["verifiedEphemerals"] = [ Data(matches[i].matchEpoc).hex(), Data(matches[j].matchEpoc).hex() ]
                            matchObject["lat"] = matches[i].contact.lat
                            matchObject["lon"] = matches[i].contact.lon
                            matchObject["contactIntegrityLevel"] = "high"
                            
                            matchesResultArray.append(matchObject)
                            //                        return "Found match for time: \(matches[i].contact.timestamp) with epoch: \(matches[i].matchEpoc) and: \(matches[j].contact.timestamp) with epoch: \(matches[j].matchEpoc)"
                        }
                        else if isSuccessiveEpoch(anchorMatch: matches[i],
                                                  targetEpoch: matches[j].matchEpoc,
                                                  infectedEpochs: infectedEpochs,
                                                  reportStartDay: startDay)
                        {
                            var matchObject : [String:Any] = [:]
                            matchObject["startContactTimestamp"] = matches[i].contact.timestamp
                            matchObject["endContactTimestamp"] = matches[j].contact.timestamp
                            matchObject["verifiedEphemerals"] = [ Data(matches[i].matchEpoc).hex(), Data(matches[j].matchEpoc).hex() ]
                            matchObject["lat"] = matches[i].contact.lat
                            matchObject["lon"] = matches[i].contact.lon
                            
                            matchObject["contactIntegrityLevel"] = "low"
                            
                            matchesResultArray.append(matchObject)
                            //                        return "Found match for time: \(matches[i].contact.timestamp) with epoch: \(matches[i].matchEpoc) and: \(matches[j].contact.timestamp) with epoch: \(matches[j].matchEpoc)"
                        }
                    }
                }
            }
        }

        let jsonData = try! JSONSerialization.data(withJSONObject: matchesResultArray, options: .prettyPrinted)

        return String(data: jsonData, encoding: .utf8) ?? ""
    }
    
    func createNewUserAndSave() {
        let masterKey = [UInt8](Data(randomOfLength: 16))
        let userKey = [UInt8](Data(randomOfLength: 16))
        let init_time = Int(Date().timeIntervalSince1970)
        print("masterKey: \(masterKey)")
        print("userKey: \(userKey)")
        print("init_time: \(init_time)")
        let user = User(user_id: userKey, master_key: masterKey, init_time: init_time)
        writeUserToDefaults(user: user, key: "mySelf")
        mySelf = user
    }
    
    fileprivate func isSuccessiveEpoch(anchorMatch: Match, targetEpoch: [UInt8],  infectedEpochs:[Int: [Int : [[UInt8]]]], reportStartDay: Int ) -> Bool {
        let t = Time(anchorMatch.contact.timestamp)
        var hour = t.epoch
        var day = t.day
//        var day = t.day - reportStartDay // we need to know the relative location of the exmined day as sent from MOH
        if hour == 0 {
            hour = 23
            day -= 1
            if day - reportStartDay < 0 {
                return false
            }
        } else {
            hour -= 1
        }
        
        if let prevEpoch = infectedEpochs[day]![hour] {
            return prevEpoch.contains(targetEpoch)
        }
        return false
    }
    
    fileprivate func parseInfectedDbToJSONString (infetedDB: [Int: [Int : [[UInt8]]]]) -> String {
        var root:[String:Any] = [:]
        var rootInfected:[Any] = []
        var resultJson = "{}"
        
        if infetedDB.keys.count > 0 {
            let keySetArray = infetedDB.keys.sorted { $0 < $1 }
            let today = keySetArray.last
            let startDay = today! - 14 // We subtract 14 from today, because we want to go 15 days back and today is the 15th day.
            root["startDay"] = startDay
            
            for i in startDay...today! {
                var rootInfectedEpochs:[[String]] = []
                if let epochs = infetedDB[i] {
                    let epochKeySetArray:[Int] = Array(epochs.keys)
                    
                    for x in 0..<24 {
                        var rootInfectedEpochsInnerLevel:[String] = []
                        let epocKey = x < epochKeySetArray.count ? epochKeySetArray[x] : -1
                        if epocKey != -1 {
                            if let ephs = epochs[epocKey] {
                                for j in 0..<ephs.count {
                                    let converted = Data(ephs[j]).hex()
                                    rootInfectedEpochsInnerLevel.append(converted)
                                }
                            }
                        }
                        rootInfectedEpochs.append(rootInfectedEpochsInnerLevel)
                    }
                } else {
                    for _ in 0..<24 {
                        let rootInfectedEpochsInnerLevel:[String] = []
                        rootInfectedEpochs.append(rootInfectedEpochsInnerLevel)
                    }
                }
                rootInfected.append(rootInfectedEpochs)
            }
            root["days"] = rootInfected
        }
        
        let dataJson = try! JSONSerialization.data(withJSONObject: root, options: .prettyPrinted)
        resultJson = String(data: dataJson, encoding: .utf8) ?? "{}"
        print(resultJson)
        return resultJson
    }
}

// MARK:- Extentions
extension Data {
    func hex() -> String {
           return map { String(format: "%02hhx", $0) }.joined()
    }
    
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
