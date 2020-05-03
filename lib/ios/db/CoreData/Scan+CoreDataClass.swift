//
//  Scan+CoreDataClass.swift
//  
//
//  Created by Tzufit Lifshitz on 4/20/20.
//
//

import Foundation
import CoreData

@objc(Scan)
public class Scan: CoreDataCodable {
    
    enum CodingKeys: Any, CodingKey {
        case public_key
        case scan_address
        case scan_protocol
        case device_rssi
        case timestamp
        case device_tx
    }
    
    // MARK: - Encodable
    public override func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(public_key, forKey: .public_key)
        try container.encode(scan_address, forKey: .scan_address)
        try container.encode(scan_protocol, forKey: .scan_protocol)
        try container.encode(device_rssi, forKey: .device_rssi)
        try container.encode(timestamp, forKey: .timestamp)
        try container.encode(device_tx, forKey: .device_tx)
    }

}
