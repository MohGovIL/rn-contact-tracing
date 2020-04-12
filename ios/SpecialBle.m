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


@implementation SpecialBle

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
  return @[@"scanningStatus", @"advertisingStatus", @"foundDevice"];
}

RCT_EXPORT_METHOD(startBLEScan:(NSString *) serviceUUID) {
  [[SpecialBleManager sharedManager] scan:serviceUUID withEventEmitter:self];
}

RCT_EXPORT_METHOD(stopBLEScan) {
  RCTLogInfo(@"stopBLEScan TBD");
}

RCT_EXPORT_METHOD(advertise:(NSString *) serviceUUID data:(NSString*)data) {
  [[SpecialBleManager sharedManager] advertise:serviceUUID withEventEmitter:self];
}

RCT_EXPORT_METHOD(startBLEService:(NSString *) serviceUUID interval:(int) interval) {
  RCTLogInfo(@"startBLEService TBD");
}


RCT_EXPORT_METHOD(stopBLEService) {
  RCTLogInfo(@"stopBLEService TBD");
}
@end
