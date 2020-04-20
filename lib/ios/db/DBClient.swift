//
//  DBClient.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/19/20.
//

import Foundation
import CoreData

@objc(DBClient)
class DBClient: NSObject {
    
    @objc(cleanDevicesDB)
    static func cleanDevicesDB() {
        DBDeviceManager.shared.deleteAllDevices()
    }
    
    @objc(setPublicKeys:)
    static func setPublicKeys(keys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: keys)
    }
    
    @objc(getAllDevices)
    static func getAllDevices() -> [NSManagedObject] {
        return DBDeviceManager.shared.getAllDevices()
    }
}
