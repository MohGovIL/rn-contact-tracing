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
        DBManager.shared.save(entity: "Scan", attributes: scanInfo)
    }
    
    func updateScan(scanInfo: [String:Any]) {
        DBManager.shared.updateScan(attributes: scanInfo)
    }
    
    func deleteAllScans() {
        DBManager.shared.deleteAllData("Scan")
    }
    
}
