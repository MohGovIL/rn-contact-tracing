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
    
    public static func insertAllKeys(publicKeys: [String]) {
        DBPublicKeyManager.shared.savePublicKeys(keys: publicKeys)
    }

    /***********
     * Devices *
     ***********/
    public static func getDeviceByKey(publicKey: String) -> Device {
        return DBDeviceManager.shared.getDeviceByKey(publicKey: publicKey);
    }

    public static func updateDevice(device: Device){
//        bleDevicesDB.deviceDao().update(device);
    }

    public static func addDevice(newDevice: Device){
//        bleDevicesDB.deviceDao().insert(newDevice);
    }

    public static func getAllDevices() -> [Device] {
        return DBDeviceManager.shared.getAllDevices()
    }

    @objc(getAllDevices)
    public static func clearAllDevices() {
//        bleDevicesDB.deviceDao().clearAll();
    }

    /***********
     *  Scans  *
     ***********/
    public static func getScanByKey(publicKey: String) -> Scan {
        return DBScanManager.shared.getScanByKey(publicKey: publicKey)
    }

    public static func updateScan(scan: Scan){
//        bleDevicesDB.scanDao().update(scan);
    }

    public static func addScan(newScan: Scan){
//        bleDevicesDB.scanDao().insert(newScan);
    }

    public static func getAllScans() -> [Scan] {
       return DBScanManager.shared.getAllScans()
    }

    public static func clearAllScans() {
//        bleDevicesDB.scanDao().clearAll();
    }
    
}
