//
//  BLEManager.h
//  BLETest
//
//  Created by Ran Greenberg on 07/04/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <CoreBluetooth/CoreBluetooth.h>
#import <React/RCTEventEmitter.h>



@interface SpecialBleManager : NSObject <CBCentralManagerDelegate>

+ (id)sharedManager;
- (void)startBLEServicesWithEventEmitter:(RCTEventEmitter*)emitter;
- (void)stopBLEServicesWithEmitter:(RCTEventEmitter*)emitter;
- (void)scan:(NSString *)serviceUUIDString withEventEmitter:(RCTEventEmitter*)emitter;
- (void)stopScan:(RCTEventEmitter*)emitter;
- (void)advertise:(NSString *)serviceUUIDString publicKey:(NSString*)publicKey withEventEmitter:(RCTEventEmitter*)emitter;
- (void)stopAdvertise:(RCTEventEmitter*)emitter;

- (NSString*)findMatchForInfections:(NSString*)jsonString;
- (void) writeContactsDB:(NSString*)jsonString;

- (void)keepAliveBLEStartForTask:(NSString*)taskName;

@end
