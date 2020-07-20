//
//  BLEModule.m
//  BLETest
//
//  Created by Ran Greenberg on 07/04/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <CoreBluetooth/CoreBluetooth.h>

#import "SpecialBle.h"
#import <React/RCTLog.h>
#import "SpecialBleManager.h"
#import "Config.h"
#import "rn_contact_tracing-Swift.h"

@implementation SpecialBle

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
  return @[@"scanningStatus", @"advertisingStatus", @"foundDevice", @"foundScan"];
}

// *** API FOR HAMAGEN *** //

#pragma mark - BLE services and tasks

RCT_EXPORT_METHOD(startBLEService) {
    [[SpecialBleManager sharedManager] startBLEServicesWithEventEmitter:self];
}

RCT_EXPORT_METHOD(stopBLEService) {
    [[SpecialBleManager sharedManager] stopBLEServicesWithEmitter:self];
}

RCT_EXPORT_METHOD(deleteDatabase) {
    [DBClient clearAllDevices];
    [DBClient clearAllScans];
    [DBClient clearAllContacts];
}

RCT_EXPORT_METHOD(match:(NSString *)jsonString callback:(RCTResponseSenderBlock)callback) {
    callback(@[[[SpecialBleManager sharedManager] findMatchForInfections:jsonString]]);
}

RCT_EXPORT_METHOD(fetchInfectionDataByConsent:(RCTResponseSenderBlock)callback) {
    callback(@[[CryptoClient fetchInfectionDataByConsent]]);
}

RCT_EXPORT_METHOD(writeContactsToDB:(NSString *)jsonString) {
    [[SpecialBleManager sharedManager] writeContactsDB:jsonString];
}

#pragma mark - Config

RCT_EXPORT_METHOD(getConfig:(RCTResponseSenderBlock)callback) {
    callback(@[[Config GetConfig]]);
}

RCT_EXPORT_METHOD(setConfig:(NSDictionary*)config) {
    [Config SetConfig:config];
}

#pragma mark - Exports

RCT_EXPORT_METHOD(exportAllContactsAsCsv:(RCTResponseSenderBlock)callback) {
    callback(@[[DBClient exportContactsCSV]]);
}

RCT_EXPORT_METHOD(exportAdvertiseAsCsv:(RCTResponseSenderBlock)callback) {
    callback(@[[DBClient exportAdvertismentsCSV]]);
}


// ***** Aditional methods ***** //

RCT_EXPORT_METHOD(startBLEScan:(NSString *) serviceUUID) {
  [[SpecialBleManager sharedManager] scan:serviceUUID withEventEmitter:self];
}

RCT_EXPORT_METHOD(stopBLEScan) {
    [[SpecialBleManager sharedManager] stopScan:self];
}

RCT_EXPORT_METHOD(advertise:(NSString *) serviceUUID data:(NSString*)data) {
  [[SpecialBleManager sharedManager] advertise:serviceUUID publicKey:data withEventEmitter:self];
}

RCT_EXPORT_METHOD(stopAdvertise) {
    [[SpecialBleManager sharedManager] stopAdvertise:self];
}

// TODO: remove this method?
RCT_EXPORT_METHOD(setPublicKeys:(NSArray*)keys) {
//    [DBClient savePublicKeys:keys];
}




#pragma mark - Devices

/***********
 * Devices *
 ***********/
RCT_EXPORT_METHOD(getAllDevices:(RCTResponseSenderBlock)callback) {
    NSArray *array = [DBClient getAllDevices];
    callback(@[[NSNull null], array]);
}

RCT_EXPORT_METHOD(cleanDevicesDB) {
    [DBClient clearAllDevices];
}

#pragma mark - Scans

/***********
 *  Scans  *
 ***********/
RCT_EXPORT_METHOD(getScansByKey:(NSString*)publicKey scan:(RCTResponseSenderBlock)callback) {
    NSArray *array = [DBClient getScanByKey:publicKey];
    callback(@[array]);
}

RCT_EXPORT_METHOD(getAllScans:(RCTResponseSenderBlock)callback) {
    callback([DBClient getAllScans]);
}

RCT_EXPORT_METHOD(cleanScansDB) {
    [DBClient clearAllScans];
}

@end
