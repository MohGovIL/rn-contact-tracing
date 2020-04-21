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
#import "rn_contact_tracing-Swift.h"

@implementation SpecialBle

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
  return @[@"scanningStatus", @"advertisingStatus", @"foundDevice"];
}

RCT_EXPORT_METHOD(startBLEScan:(NSString *) serviceUUID) {
  [[SpecialBleManager sharedManager] scan:serviceUUID withEventEmitter:self];
}

RCT_EXPORT_METHOD(stopBLEScan) {
    [[SpecialBleManager sharedManager] stopScan:self];
}

RCT_EXPORT_METHOD(advertise:(NSString *) serviceUUID data:(NSString*)data) {
  [[SpecialBleManager sharedManager] advertise:serviceUUID withEventEmitter:self];
}

RCT_EXPORT_METHOD(stopAdvertise) {
    [[SpecialBleManager sharedManager] stopAdvertise:self];
}

RCT_EXPORT_METHOD(startBLEService:(NSString *) serviceUUID interval:(int) interval) {
  RCTLogInfo(@"startBLEService TBD");
}


RCT_EXPORT_METHOD(stopBLEService) {
  RCTLogInfo(@"stopBLEService TBD");
}

RCT_EXPORT_METHOD(insertAllKeys:(NSArray*) publicKeys) {
    
}

/***********
 * Devices *
 ***********/
RCT_EXPORT_METHOD(getDeviceByKey:(NSString*) publicKey device:(void(^)(Device*))callback) {
    callback([DBClient getDeviceByKey:publicKey]);
}

RCT_EXPORT_METHOD(device:(Device*) device) {
    [DBClient updateDevice:device];
}

RCT_EXPORT_METHOD(addDevice:(NSDictionary*) deviceInfo) {
    [DBClient addDevice:deviceInfo];
}

RCT_EXPORT_METHOD(getAllDevices:(void(^)(NSArray*))callback) {
    callback([DBClient getAllDevices]);
}

RCT_EXPORT_METHOD(clearAllDevices) {
    [DBClient clearAllDevices];
}

/***********
 *  Scans  *
 ***********/
RCT_EXPORT_METHOD(getScanByKey:(NSString*) publicKey scan:(void(^)(Scan*))callback) {
    callback([DBClient getScanByKey:publicKey]);
}

RCT_EXPORT_METHOD(updateScan:(Scan*) scan) {
    [DBClient updateScan:scan];
}

RCT_EXPORT_METHOD(scanInfo:(NSDictionary*) scanInfo) {
    [DBClient addScan:scanInfo];
}

RCT_EXPORT_METHOD(getAllScans:(void(^)(NSArray*))callback) {
    callback([DBClient getAllScans]);
}

RCT_EXPORT_METHOD(clearAllScans) {
    [DBClient clearAllScans];
}

@end
