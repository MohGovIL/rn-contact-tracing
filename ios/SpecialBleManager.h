//
//  BLEManager.h
//  BLETest
//
//  Created by Ran Greenberg on 07/04/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <CoreBluetooth/CoreBluetooth.h>
#import <React/RCTEventEmitter.h>



@interface SpecialBleManager : NSObject <CBCentralManagerDelegate, CBPeripheralDelegate>

+ (id)sharedManager;
- (void)scan:(NSString *)serviceUUIDString withEventEmitter:(RCTEventEmitter*)emitter;
- (void)advertise:(NSString *)serviceUUIDString withEventEmitter:(RCTEventEmitter*)emitter;
@end
