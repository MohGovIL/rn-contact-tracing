//
//  DBScanManager.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/20/20.
//

import Foundation

class DBScanManager {
    
    static let shared = DBScanManager()
    
    func getScanByKey(publicKey:String) -> NSArray {
        return DBManager.shared.getEntityWithPredicate(entity: "Scan", predicateKey: "public_key", predicateValue: publicKey)
    }
    
    func getAllScans() -> NSArray {
        return DBManager.shared.getAll("Scan")
    }

    func saveNewScan(scanInfo: [String:Any]) {
        let context = DBManager.shared.persistentContainer.viewContext
        let scan = Scan(context: context)
        scan.public_key = scanInfo["public_key"] as? String
        scan.scan_address = scanInfo["scan_address"] as? String
        scan.scan_protocol = scanInfo["scan_protocol"] as? String
        scan.device_rssi = scanInfo["device_rssi"] as! Int16
        scan.timestamp = scanInfo["timestamp"] as! Int16
        scan.device_tx = scanInfo["device_tx"] as! Int16
        do {
            try context.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func updateScan() {
        let context = DBManager.shared.persistentContainer.viewContext
        do {
            try context.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    func deleteAllScans() {
        DBManager.shared.deleteAllData("Scan")
    }
    
}
