//
//  BluetoothHandlerBridge.m
//  BluetoothDemo
//
//  Created by Tzufit Lifshitz on 4/14/20.
//  Copyright © 2020 Wojciech Kulik. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(BluetoothHandler, NSObject)

RCT_EXTERN_METHOD(configureDatabase)

RCT_EXTERN_METHOD(configureLocalNotifications)

RCT_EXTERN_METHOD(configureBluetrace)

@end
