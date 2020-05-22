//
//  Config.h
//  rn-contact-tracing
//
//  Created by Ran Greenberg on 23/04/2020.
//

#import <Foundation/Foundation.h>


NS_ASSUME_NONNULL_BEGIN
static long DEFAULT_SCAN_INTERVAL = 2 * 1000L;
static long DEFAULT_SCAN_DURATION = 5 * 1000L;
static long DEFAULT_ADVERTISE_INTERVAL = 3 * 1000L;
static long DEFAULT_ADVERTISE_DURATION = 7 * 1000L;
static NSString* DEFAULT_SERVICE_UUID = @"00000000-0000-1000-8000-00805F9B34FB";
static NSString* DEFAULT_TOKEN = @"1234-ios-token";

static NSString* KEY_SERVICE_UUID = @"serviceUUID";
static NSString* KEY_TOKEN = @"token";
static NSString* KEY_SCAN_DURATION = @"scanDuration";
static NSString* KEY_SCAN_INTERVAL = @"scanInterval";
static NSString* KEY_ADVERTISE_DURATION = @"advertiseDuration";
static NSString* KEY_ADVERTISE_INTERVAL = @"advertiseInterval";


@interface Config : NSObject

+(void)SetConfig:(NSDictionary*) configDict;
+(NSDictionary*)GetConfig;

@end

NS_ASSUME_NONNULL_END
