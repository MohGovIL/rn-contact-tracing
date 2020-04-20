//
//  DBClientBridge.m
//  rn-contact-tracing
//
//  Created by Tzufit Lifshitz on 4/20/20.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(DBClient, NSObject)

RCT_EXTERN_METHOD(cleanDevicesDB)

RCT_EXTERN_METHOD(setPublicKeys:)

RCT_EXTERN_METHOD(getAllDevices)

@end
