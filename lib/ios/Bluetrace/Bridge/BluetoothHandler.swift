//
//  BluetoothHandler.swift
//  BluetoothDemo
//
//  Created by Tzufit Lifshitz on 4/14/20.
//  Copyright © 2020 Wojciech Kulik. All rights reserved.
//

import UIKit

@objc(BluetoothHandler)
class BluetoothHandler: NSObject {
    
    static let shared = BluetoothHandler()
    
    @objc(configureDatabase)
    func configureDatabase() {
        ConfigureDatabaseManager.shared().configureDatabaseManager()
    }
    
    @objc(configureLocalNotifications)
    func configureLocalNotifications() {
        BlueTraceLocalNotifications.shared.initialConfiguration()
    }
    
    @objc(configureBluetrace)
    func configureBluetrace() {
        BluetraceManager.shared.turnOn()
    }
}
