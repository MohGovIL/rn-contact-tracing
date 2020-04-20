//
//  DBClient.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/19/20.
//

import Foundation
import CoreData

@objc(DBClient)
public class DBClient: NSObject {
    
    @objc(cleanDevicesDB)
    public static func cleanDevicesDB() {
        DBDeviceManager.shared.deleteAllDevices()
    }
    
    @objc(setPublicKeys:)
    public static func setPublicKeys(keys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: keys)
    }
    
    @objc(getAllDevices)
    public static func getAllDevices() -> [NSManagedObject] {
        return DBDeviceManager.shared.getAllDevices()
    }
}
