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
    
    func saveNewDevice(deviceInfo: [String:Any]) {
        let context = DBManager.shared.persistentContainer.viewContext
        let device = Device(context: context)
        device.publicKey = deviceInfo["publicKey"] as? String
        device.device_address = deviceInfo["device_address"] as? String
        device.device_protocol = deviceInfo["device_protocol"] as? String
        device.rssi = deviceInfo["rssi"] as! Int16
        device.firstTimestamp = deviceInfo["firstTimestamp"] as! Int16
        device.lastTimestamp = deviceInfo["lastTimestamp"] as! Int16
        device.tx = deviceInfo["tx"] as! Int16
        do {
            try context.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func updateDevice() {
        let context = DBManager.shared.persistentContainer.viewContext
        do {
            try context.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func deleteAllDevices() {
        DBManager.shared.deleteAllData("Device")
    }
}
