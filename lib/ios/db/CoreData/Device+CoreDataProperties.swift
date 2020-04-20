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
    
    convenience init(publicKey: String, device_address: String, device_protocol: String, rssi: Int16, timestamp: Int16, tx: Int16, entity: NSEntityDescription, insertIntoManagedObjectContext context: NSManagedObjectContext!) {
        let entity = NSEntityDescription.entity(forEntityName: "Device", in: context)!
        self.init(entity: entity, insertIntoManagedObjectContext: context)
        self.text = text
        self.isCorrect = isCorrect
    }

    @NSManaged public var publicKey: String?
    @NSManaged public var device_address: String?
    @NSManaged public var device_protocol: String?
    @NSManaged public var rssi: Int16
    @NSManaged public var timestamp: Int16
    @NSManaged public var tx: Int16

}
