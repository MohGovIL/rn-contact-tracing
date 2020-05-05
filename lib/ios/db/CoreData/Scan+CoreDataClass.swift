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
        case scan_id
        case public_key
        case scan_address
        case scan_protocol
        case scan_rssi
        case scan_timestamp
        case scan_tx
    }
    
    // MARK: - Encodable
    public override func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(scan_id, forKey: .scan_id)
        try container.encode(public_key, forKey: .public_key)
        try container.encode(scan_address, forKey: .scan_address)
        try container.encode(scan_protocol, forKey: .scan_protocol)
        try container.encode(scan_rssi, forKey: .scan_rssi)
        try container.encode(scan_timestamp, forKey: .scan_timestamp)
        try container.encode(scan_tx, forKey: .scan_tx)
    }

}
