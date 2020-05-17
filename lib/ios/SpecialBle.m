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

#pragma mark - BLE services and tasks

// *** API FOR HAMAGEN *** //

RCT_EXPORT_METHOD(startBLEService) {
    [[SpecialBleManager sharedManager] startBLEServicesWithEventEmitter:self];
}

RCT_EXPORT_METHOD(stopBLEService) {
    [[SpecialBleManager sharedManager] stopBLEServicesWithEmitter:self];
}

RCT_EXPORT_METHOD(deleteDatabase) {
    [DBClient clearAllDevices];
    [DBClient clearAllScans];
}

RCT_EXPORT_METHOD(match:(NSString *) jsonString) {
    [[SpecialBleManager sharedManager] findMatchForInfections];
}

RCT_EXPORT_METHOD(fetchInfectionDataByConsent) {
    // TBD
}

RCT_EXPORT_METHOD(writeContactsToDB:(NSString *) jsonString) {
    [[SpecialBleManager sharedManager] writeContactsDB];
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

#pragma mark - Config

RCT_EXPORT_METHOD(getConfig:(RCTResponseSenderBlock)callback) {
    callback(@[[Config GetConfig]]);
}

RCT_EXPORT_METHOD(setConfig:(NSDictionary*)config) {
    [Config SetConfig:config];
}

#pragma mark - Exports

RCT_EXPORT_METHOD(exportAllDevicesCsv) {
//    RCTLogInfo(@"exportAllDevicesCsv TBD");
}

RCT_EXPORT_METHOD(exportAllScansCsv) {
//    RCTLogInfo(@"exportAllScansCsv TBD");
}

#pragma mark - Devices

/***********
 * Devices *
 ***********/
//RCT_EXPORT_METHOD(getDeviceByKey:(NSString*) publicKey device:(void(^)(Device*))callback) {
//    callback([DBClient getDeviceByKey:publicKey]);
//}

//RCT_EXPORT_METHOD(device:(Device*) device) {
//    [DBClient updateDevice:device];
//}

//RCT_EXPORT_METHOD(addDevice:(NSDictionary*) deviceInfo) {
//    [DBClient addDevice:deviceInfo];
//}

RCT_EXPORT_METHOD(getAllDevices:(RCTResponseSenderBlock)callback) {
    NSArray *array = [DBClient getAllDevices];
    callback(@[[NSNull null], array]);
}

RCT_EXPORT_METHOD(cleanDevicesDB) {
    [DBClient clearAllDevices];
}

RCT_EXPORT_METHOD(addDemoDevice) {
    NSDate *date = [NSDate date]; // current date
    int unixtime = [date timeIntervalSince1970];
    NSDictionary* demoDevice = @{
        @"public_key": [SpecialBle randomStringWithLength:8],
        @"device_rssi": [NSNumber numberWithInt:555],
        @"device_first_timestamp": [NSNumber numberWithInt:unixtime],
        @"device_last_timestamp": [NSNumber numberWithInt:unixtime],
        @"device_tx": [NSNumber numberWithInt:0]
    };
    [DBClient addDevice:demoDevice];
}

NSString *letters = @"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

+(NSString *) randomStringWithLength: (int) len {

    NSMutableString *randomString = [NSMutableString stringWithCapacity: len];

    for (int i=0; i<len; i++) {
         [randomString appendFormat: @"%C", [letters characterAtIndex: arc4random_uniform([letters length])]];
    }

    return randomString;
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
