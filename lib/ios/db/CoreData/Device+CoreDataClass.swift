//
//  Device+CoreDataClass.swift
//  
//
//  Created by Tzufit Lifshitz on 4/20/20.
//
//  This file was automatically generated and should not be edited.
//

import Foundation
import CoreData

@objc(Device)
public class Device: CoreDataCodable {

    enum CodingKeys: Any, CodingKey {
        case public_key
        case device_address
        case device_protocol
        case device_rssi
        case device_first_timestamp
        case device_last_timestamp
        case device_tx
        case last_connected_timestamp
    }
    
    // MARK: - Decodable
//    required convenience public init(from decoder: Decoder) throws {
//        guard let codingUserInfoKeyManagedObjectContext = CodingUserInfoKey.managedObjectContext,
//            let managedObjectContext = decoder.userInfo[codingUserInfoKeyManagedObjectContext] as? NSManagedObjectContext,
//            let entity = NSEntityDescription.entity(forEntityName: "Device", in: managedObjectContext) else {
//            fatalError("Failed to decode Device")
//        }
//
//        self.init(entity: entity, insertInto: managedObjectContext)
//
//        let container = try decoder.container(keyedBy: CodingKeys.self)
//        self.publicKey = try container.decodeIfPresent(String.self, forKey: .publicKey)
//        self.device_address = try container.decodeIfPresent(String.self, forKey: .device_address)
//        self.device_protocol = try container.decodeIfPresent(String.self, forKey: .device_protocol)
//        self.rssi = try container.decodeIfPresent(Int16.self, forKey: .rssi)!
//        self.firstTimestamp = try container.decodeIfPresent(Int16.self, forKey: .firstTimestamp)!
//        self.lastTimestamp = try container.decodeIfPresent(Int16.self, forKey: .lastTimestamp)!
//        self.tx = try container.decodeIfPresent(Int16.self, forKey: .tx)!
//    }

    // MARK: - Encodable
    public override func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(public_key, forKey: .public_key)
        try container.encode(device_address, forKey: .device_address)
        try container.encode(device_protocol, forKey: .device_protocol)
        try container.encode(device_rssi, forKey: .device_rssi)
        try container.encode(device_first_timestamp, forKey: .device_first_timestamp)
        try container.encode(device_last_timestamp, forKey: .device_last_timestamp)
        try container.encode(device_tx, forKey: .device_tx)
        try container.encode(last_connected_timestamp, forKey: .last_connected_timestamp)
    }
}
