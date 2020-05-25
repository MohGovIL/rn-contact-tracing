//
//  Contact+CoreDataProperties.swift
//  rn-contact-tracing
//
//  Created by Yonatan Rimon on 13/05/2020.
//

import Foundation
import CoreData


extension Contact {

    @nonobjc public class func fetchRequest() -> NSFetchRequest<Contact> {
        return NSFetchRequest<Contact>(entityName: "Contact")
    }

    @NSManaged public var ephemeral_id: Data
    @NSManaged public var geohash: Data
    @NSManaged public var id: Int32
    @NSManaged public var rssi: Int16
    @NSManaged public var timestamp: Int
    @NSManaged public var lat: Double
    @NSManaged public var lon: Double
}
