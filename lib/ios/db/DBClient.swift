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
    
    @objc(savePublicKeys:)
    public static func savePublicKeys(publicKeys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: publicKeys)
    }

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

    /***********
     *  Scans  *
     ***********/
    @objc(getScanByKey:)
    public static func getScanByKey(publicKey: String) -> NSArray {
        return DBScanManager.shared.getScanByKey(publicKey: publicKey)
    }

    @objc(updateScan:)
    public static func updateScan(scan: Scan){
        DBScanManager.shared.updateScan()
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
