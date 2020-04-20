//
//  DBDeviceManager.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/19/20.
//

import Foundation
import CoreData

class DBDeviceManager {
    
    static let shared = DBDeviceManager()
    
    func getDeviceByKey(publicKey:String) -> Device {
        return DBManager.shared.getEntityWithPredicate(entity: "Device", predicateKey: "publicKey", predicateValue: publicKey) as! Device
    }

    func getAllDevices() -> [Device] {
        return DBManager.shared.getAll("Device") as! [Device]
    }
    
    func deleteAllDevices() {
        DBManager.shared.deleteAllData("Device")
    }
}
