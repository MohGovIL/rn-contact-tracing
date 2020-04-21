//
//  DBScanManager.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/20/20.
//

import Foundation

class DBScanManager {
    
    static let shared = DBScanManager()
    
    func getScanByKey(publicKey:String) -> Scan {
        return DBManager.shared.getEntityWithPredicate(entity: "Scan", predicateKey: "publicKey", predicateValue: publicKey) as! Scan
    }
    
    func getAllScans() -> [Scan] {
        return DBManager.shared.getAll("Scan") as! [Scan]
    }

    func saveNewScan(scanInfo: [String:Any]) {
        let context = DBManager.shared.persistentContainer.viewContext
        let scan = Scan(context: context)
        scan.publicKey = scanInfo["publicKey"] as? String
        scan.scan_address = scanInfo["scan_address"] as? String
        scan.scan_protocol = scanInfo["scan_protocol"] as? String
        scan.rssi = scanInfo["rssi"] as! Int16
        scan.timestamp = scanInfo["timestamp"] as! Int16
        scan.tx = scanInfo["tx"] as! Int16
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
