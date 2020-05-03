//
//  PublicKey+CoreDataProperties.swift
//  
//
//  Created by Tzufit Lifshitz on 4/20/20.
//
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData


extension PublicKey {

    @nonobjc public class func fetchRequest() -> NSFetchRequest<PublicKey> {
        return NSFetchRequest<PublicKey>(entityName: "PublicKey")
    }

    @NSManaged public var id: Int16
    @NSManaged public var public_key: String?

}
