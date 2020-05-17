//
//  BytesUtils.swift
//
//  Created by Yonatan Rimon on 13/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation

// Decoding: String(data: bytes, encoding: .ascii)
let STRINGS = ["id" : [UInt8]("IdentityKey".data(using: .ascii)!),
               "id_com" : [UInt8]("IdentityCommitment".data(using: .ascii)!),
               "master0" : [UInt8]("DeriveMasterFirstKey".data(using: .ascii)!),
               "master" : [UInt8]("DeriveMasterKey".data(using: .ascii)!),
               "ddaykey" : [UInt8]("DeriveDayKey".data(using: .ascii)!),
               "dverif" : [UInt8]("DeriveVerificationKey".data(using: .ascii)!),
               "depoch" : [UInt8]("DeriveEpoch".data(using: .ascii)!),
               "verifkey" : [UInt8]("VerificationKey".data(using: .ascii)!)]

class BytesUtils {
    
    // Int to bytes array
    class func numToBytes(num: Int, numBytes: Int) -> [UInt8] {
        var res:[UInt8] = Array()
        var _number:Int = num
        for _ in 0 ..< numBytes {
            res.append(UInt8( _number&0xff ))
            _number >>= 8 //shift 8 times from left to right
        }
        // TODO: ASSERT
        // assert num == 0, "Sanity check"
        return res
    }
    
    // hex string to bytes array
    class func hexToBytes(str: String) -> [UInt8] {
        // omit error checking: remove '0x', make sure even, valid chars
        let pairs = BytesUtils.toPairsOfChars(pairs: [], string: str)
        return pairs.map { UInt8($0, radix: 16)! }
    }
    
    
    class func toPairsOfChars(pairs: [String], string: String) -> [String] {
        if string.count == 0 {
            return pairs
        }
        var pairsMod = pairs
        pairsMod.append(String(string.prefix(2)))
        return toPairsOfChars(pairs: pairsMod, string: String(string.dropFirst(2)))
    }

    // pad
    class func pad(array: [UInt8], size: Int) -> [UInt8] {
        // TODO: ASSERT
        //     assert len(array) <= size, "Padded array should not be smaller."
        if size < array.count {
            return [UInt8]()
        }
        let paddingSize = size - array.count
        let paddingArray = [UInt8](repeating: 0, count: paddingSize)

        return array + paddingArray
    }
    
    // xor
    class func xor(_ a: [UInt8],_ b: [UInt8]) -> [UInt8] {
        // TODO: ASSERT
        //     assert len(a) == len(b)      
        return zip(a, b).map{$0^$1}
    }
}
