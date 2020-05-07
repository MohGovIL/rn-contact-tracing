//
//  BytesUtil.swift
//  DoubleConversion
//
//  Created by Yonatan Rimon on 07/05/2020.
//

import Foundation


class BytesUtils {
    
    class func numToBytes(num: Int, numBytes: Int) -> [UInt8] {
        var res:[UInt8] = Array()
        var _number:Int = num
        for _ in 0 ..< numBytes {
            res.append(UInt8( _number&0xff ))
            _number >>= 8 //shift 8 times from left to right
        }
        // TODO: if num == 0 rais assertionError "Sanity check"
        return res
    }
    
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

    
}
