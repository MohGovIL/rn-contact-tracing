//
//  Device+CoreDataProperties.swift
//  
//
//  Created by Tzufit Lifshitz on 4/20/20.
//
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData


extension Device {

    @nonobjc public class func fetchRequest() -> NSFetchRequest<Device> {
        return NSFetchRequest<Device>(entityName: "Device")
    }

    @NSManaged public var public_key: String?
    @NSManaged public var device_address: String?
    @NSManaged public var device_protocol: String?
    @NSManaged public var rssi: Int16
    @NSManaged public var device_first_timestamp: Int16
    @NSManaged public var device_last_timestamp: Int16
    @NSManaged public var tx: Int16

}
