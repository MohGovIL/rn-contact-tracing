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

    @NSManaged public var scan_id: Int32
    @NSManaged public var public_key: String?
    @NSManaged public var scan_timestamp: Int64
    @NSManaged public var scan_address: String?
    @NSManaged public var scan_rssi: Int16
    @NSManaged public var scan_tx: Int16
    @NSManaged public var scan_protocol: String?

}
