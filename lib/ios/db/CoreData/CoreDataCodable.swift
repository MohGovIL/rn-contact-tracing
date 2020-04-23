//
//  CoreDataCodable.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/23/20.
//

import Foundation
import CoreData

public class CoreDataCodable: NSManagedObject, Codable {
    
    required convenience public init(from decoder: Decoder) throws {
        guard let codingUserInfoKeyManagedObjectContext = CodingUserInfoKey.managedObjectContext,
            let managedObjectContext = decoder.userInfo[codingUserInfoKeyManagedObjectContext] as? NSManagedObjectContext,
            let entity = NSEntityDescription.entity(forEntityName: "Device", in: managedObjectContext) else {
            fatalError("Failed to decode Device")
        }

        self.init(entity: entity, insertInto: managedObjectContext)
    }
    
    public func encode(to encoder: Encoder) throws {

    }
    
}
