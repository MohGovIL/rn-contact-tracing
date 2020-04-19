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

    func getAllDevices() -> [NSManagedObject] {
        return DBManager.shared.getAll("Device")
    }
    
    func deleteAllDevices() {
        DBManager.shared.deleteAllData("Device")
    }
}
