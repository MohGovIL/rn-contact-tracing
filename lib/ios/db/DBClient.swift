//
//  DBClient.swift
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/19/20.
//

import Foundation
import CoreData

@objc(DBClient)
public class DBClient: NSObject {
    
    //MARK:- Public Keys
    @objc(savePublicKeys:)
    public static func savePublicKeys(publicKeys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: publicKeys)
    }

    //MARK:- Contacts
    @objc(getContacts)
    public static func getContacts() -> NSFetchedResultsController<Contact> {
        let res = DBContactManager.shared.getContacts()
        return res
    }
    
    @objc(addContactWithAsciiEphemeral::::::)
    public static func addContactWithAsciiEphemeral(ephemeral_id: String, rssi: Int, time: Int, location: [UInt8], lat: Double, lon: Double){
        let contactsCount = DBClient.getContacts().fetchedObjects?.count ?? 0

        DBContactManager.shared.addNewContact(ephemeral_id: ephemeral_id.asciiToUInt8Bytes(), rssi: rssi, time: time, location: location, id: contactsCount+1,lat: lat, lon: lon)
    }
    
    @objc(addJsonContact::::::) // this one is with hex string ephemeral
        public static func addJsonContact(ephemeral_id: String, rssi: Int, time: Int, location: String, lat: Double, lon: Double){
            let contactsCount = DBClient.getContacts().fetchedObjects?.count ?? 0

            DBContactManager.shared.addNewContact(ephemeral_id: stringToBytes(ephemeral_id)!, rssi: rssi, time: time, location: stringToBytes(location)!, id: contactsCount+1,lat: lat, lon: lon)
        }
    
    @objc(clearAllContacts)
    public static func clearAllContacts() {
        DBContactManager.shared.deleteAllContacts()
    }
    
    
    // Hex string to bytes array
    static func stringToBytes(_ string: String) -> [UInt8]? {
        let length = string.count
        if length & 1 != 0 {
            return nil
        }
        var bytes = [UInt8]()
        bytes.reserveCapacity(length/2)
        var index = string.startIndex
        for _ in 0..<length/2 {
            let nextIndex = string.index(index, offsetBy: 2)
            if let b = UInt8(string[index..<nextIndex], radix: 16) {
                bytes.append(b)
            } else {
                return nil
            }
            index = nextIndex
        }
        return bytes
    }
    
    public static func deleteContact(_ contact: Contact) {
        DBContactManager.shared.deleteContact(contact)
    }
    
    public static func deleteContactsHistory(dtime: Int) {
        DBContactManager.shared.deleteContactsHistory(dtime: dtime)
    }
    
    
    //MARK:- Devices
    /***********
     * Devices *
     ***********/
    @objc(getDeviceByKey:)
    public static func getDeviceByKey(publicKey: String) -> NSArray {
        return DBDeviceManager.shared.getDeviceByKey(publicKey: publicKey);
    }

    @objc(updateDevice:)
    public static func updateDevice(deviceInfo: [String:Any]){
        DBDeviceManager.shared.updateDevice(deviceInfo: deviceInfo)
    }

    @objc(addDevice:)
    public static func addDevice(deviceInfo: [String:Any]){
        DBDeviceManager.shared.saveNewDevice(deviceInfo: deviceInfo)
    }

    @objc(getAllDevices)
    public static func getAllDevices() -> NSArray {
        let list:NSArray = DBDeviceManager.shared.getAllDevices() as NSArray
        return list
    }

    @objc(clearAllDevices)
    public static func clearAllDevices() {
        DBDeviceManager.shared.deleteAllDevices()
    }

    //MARK:- Scans
    /***********
     *  Scans  *
     ***********/
    @objc(getScanByKey:)
    public static func getScanByKey(publicKey: String) -> NSArray {
        return DBScanManager.shared.getScanByKey(publicKey: publicKey)
    }

    @objc(updateScan:)
    public static func updateScan(scanInfo: [String:Any]){
        DBScanManager.shared.updateScan(scanInfo: scanInfo)
    }

    @objc(addScan:)
    public static func addScan(scanInfo: [String:Any]){
        DBScanManager.shared.saveNewScan(scanInfo: scanInfo)
    }

    @objc(getAllScans)
    public static func getAllScans() -> NSArray {
       return DBScanManager.shared.getAllScans()
    }

    @objc(clearAllScans)
    public static func clearAllScans() {
        DBScanManager.shared.deleteAllScans()
    }
    
}
