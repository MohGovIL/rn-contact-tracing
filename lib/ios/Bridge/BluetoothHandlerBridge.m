//
//  BluetoothHandlerBridge.m
//  BluetoothDemo
//
//  Created by Tzufit Lifshitz on 4/14/20.
//  Copyright © 2020 Wojciech Kulik. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(DBClient, NSObject)

RCT_EXTERN_METHOD(cleanDevicesDB)

RCT_EXTERN_METHOD(setPublicKeys:)

RCT_EXTERN_METHOD(getAllDevices)

@end
