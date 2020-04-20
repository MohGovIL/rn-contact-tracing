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
    
}
