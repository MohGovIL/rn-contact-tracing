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
    
    @objc(insertAllKeys:)
    public static func insertAllKeys(publicKeys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: publicKeys)
    }

    /***********
     * Devices *
     ***********/
    @objc(getDeviceByKey:)
    public static func getDeviceByKey(publicKey: String) -> Device {
        return DBDeviceManager.shared.getDeviceByKey(publicKey: publicKey);
    }

    @objc(updateDevice:)
    public static func updateDevice(device: Device){
        DBDeviceManager.shared.updateDevice()
    }

    @objc(addDevice:)
    public static func addDevice(deviceInfo: [String:Any]){
        DBDeviceManager.shared.saveNewDevice(deviceInfo: deviceInfo)
    }

    @objc(getAllDevices)
    public static func getAllDevices() -> [Device] {
        return DBDeviceManager.shared.getAllDevices()
    }

    @objc(clearAllDevices)
    public static func clearAllDevices() {
        DBDeviceManager.shared.deleteAllDevices()
    }

    /***********
     *  Scans  *
     ***********/
    @objc(getScanByKey:)
    public static func getScanByKey(publicKey: String) -> Scan {
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
    public static func getAllScans() -> [Scan] {
       return DBScanManager.shared.getAllScans()
    }

    @objc(clearAllScans)
    public static func clearAllScans() {
        DBScanManager.shared.deleteAllScans()
    }
    
}
