//
//  PublicKey+CoreDataClass.swift
//  
//
//  Created by Tzufit Lifshitz on 4/20/20.
//
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData

@objc(PublicKey)
public class PublicKey: CoreDataCodable {
    
    enum CodingKeys: Any, CodingKey {
        case id
        case public_key
    }
    
    // MARK: - Encodable
    public override func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(public_key, forKey: .public_key)
    }

}
