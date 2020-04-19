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
    func cleanDevicesDB() {
        DBDeviceManager.shared.deleteAllDevices()
    }
    
    @objc(setPublicKeys:)
    func setPublicKeys(keys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: keys)
    }
    
    @objc(getAllDevices)
    func getAllDevices() -> [NSManagedObject] {
        return DBDeviceManager.shared.getAllDevices()
    }
}
