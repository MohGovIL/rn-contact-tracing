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
        case publicKey
        case scan_address
        case scan_protocol
        case rssi
        case timestamp
        case tx
    }
    
    // MARK: - Encodable
    public override func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(publicKey, forKey: .publicKey)
        try container.encode(scan_address, forKey: .scan_address)
        try container.encode(scan_protocol, forKey: .scan_protocol)
        try container.encode(rssi, forKey: .rssi)
        try container.encode(timestamp, forKey: .timestamp)
        try container.encode(tx, forKey: .tx)
    }

}
