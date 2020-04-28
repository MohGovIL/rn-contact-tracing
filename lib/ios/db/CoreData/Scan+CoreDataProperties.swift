//
//  Scan+CoreDataProperties.swift
//  
//
//  Created by Tzufit Lifshitz on 4/20/20.
//
//

import Foundation
import CoreData


extension Scan {

    @nonobjc public class func fetchRequest() -> NSFetchRequest<Scan> {
        return NSFetchRequest<Scan>(entityName: "Scan")
    }

    @NSManaged public var publicKey: String?
    @NSManaged public var timestamp: Int16
    @NSManaged public var scan_address: String?
    @NSManaged public var device_rssi: Int16
    @NSManaged public var device_tx: Int16
    @NSManaged public var scan_protocol: String?

}
