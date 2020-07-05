//
//  Crypto.swift
//
//  Created by Yonatan Rimon on 10/05/2020.
//  Copyright © 2020 iGates. All rights reserved.
//

import Foundation
import CommonCrypto

//enum AESError: Error {
//    case KeyError((String, Int))
//    case IVError((String, Int))
//    case CryptorError((String, Int))
//}

//enum Error: Swift.Error {
//    case encryptionError(status: CCCryptorStatus)
//    case decryptionError(status: CCCryptorStatus)
//    case keyDerivationError(status: CCCryptorStatus)
//}
let useCryptography = true
let AES_BLOCK_SIZE = 16
let KEY_LEN = 16

class Crypto {
    
    
//    Compute HMAC with SHA256 hash function.
//    :param key:     array of size 16 bytes.
//    :param data:    data to sign.
    class func hmac_sha256(key: [UInt8], data: [UInt8]) -> [UInt8] {
        if key.count != KEY_LEN { //We only support 128 bit key
            return [UInt8]()
        }
        
        let digestLen = Int(CC_SHA256_DIGEST_LENGTH)
        let result = UnsafeMutablePointer<Any>.allocate(capacity: digestLen)
        let algorithm = CCHmacAlgorithm(kCCHmacAlgSHA256)

        CCHmac(algorithm, key, key.count, data, data.count, result)
        
        let resultData = NSData(bytesNoCopy: result, length: digestLen)

        return [UInt8](resultData as Data)
    }
    
    class func hmac_sha256_firstItems(key: [UInt8], data: [UInt8], numberOfItems: Int) -> [UInt8] {
        let fullHmac = hmac_sha256(key: key, data: data)
        if (fullHmac.count < numberOfItems+1) {
            return fullHmac
        }
        return Array(fullHmac[0..<numberOfItems])
    }

            
    class func encrypt_cryptography(key: [UInt8], plain: [UInt8]) -> [UInt8] {
       
        let cryptData    = NSMutableData(length: plain.count + kCCBlockSizeAES128)!
        
        let keyLength              = size_t(kCCKeySizeAES128)
        let operation: CCOperation = UInt32(kCCEncrypt)
        let algoritm:  CCAlgorithm = UInt32(kCCAlgorithmAES128)
        let options:   CCOptions   = UInt32(kCCOptionECBMode)
        
        var numBytesEncrypted :size_t = 0
        
        
        let cryptStatus = CCCrypt(operation,
                                  algoritm,
                                  options,
                                  key, keyLength,
                                  nil,
                                  plain, plain.count,
                                  cryptData.mutableBytes, cryptData.length,
                                  &numBytesEncrypted)
        
        if UInt32(cryptStatus) == UInt32(kCCSuccess) {
            cryptData.length = Int(numBytesEncrypted)
            
            var bytes = [UInt8](repeating: 0, count: cryptData.length)
            cryptData.getBytes(&bytes, length: cryptData.length)
            
            return bytes
        }
        
        return [UInt8]()
    }
    
//    Encrypt a block with AES encryption.
//    :param key:     array of size 16 bytes.
//    :param plain:   array of size 16 bytes.
//    :return:        the encrypted array is size 16 bytes.
    class func encrypt(key: [UInt8], plain: [UInt8]) -> [UInt8]
    {
        if key.count != KEY_LEN || plain.count != KEY_LEN {
            return [UInt8]()
        }
        
//    assert len(key) == KEY_LEN, "We only support 128 bit key, but len(key) = {})".format(len(key))
//    assert len(plain) == KEY_LEN, "We only support 128 bit key, but len(key) = {})".format(len(key))

        return encrypt_cryptography(key: key, plain: plain)
    }
}


