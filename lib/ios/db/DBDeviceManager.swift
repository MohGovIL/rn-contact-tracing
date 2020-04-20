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

    func getAllDevices() -> [Device] {
        return DBManager.shared.getAll("Device") as! [Device]
    }
    
    func deleteAllDevices() {
        DBManager.shared.deleteAllData("Device")
    }
}
