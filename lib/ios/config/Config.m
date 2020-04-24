//
//  Config.m
//  rn-contact-tracing
//
//  Created by Ran Greenberg on 23/04/2020.
//

#import "Config.h"

static long DEFAULT_SCAN_INTERVAL = 2 * 1000L;
static long DEFAULT_SCAN_DURATION = 5 * 1000L;
static long DEFAULT_ADVERTISE_INTERVAL = 3 * 1000L;
static long DEFAULT_ADVERTISE_DURATION = 7 * 1000L;
static NSString* DEFAULT_SERVICE_UUID = @"00000000-0000-1000-8000-00805F9B34FB";
static NSString* DEFAULT_TOKEN = @"default_public_key";

static NSString* KEY_SERVICE_UUID = @"serviceUUID";
static NSString* KEY_SCAN_DURATION = @"scanDuration";
static NSString* KEY_SCAN_INTERVAL = @"scanInterval";
static NSString* KEY_ADVERTISE_DURATION = @"advertiseDuration";
static NSString* KEY_ADVERTISE_INTERVAL = @"advertiseInterval";


@implementation Config

+(void)SetConfig:(NSDictionary*)configDict {
    [Config _save:DEFAULT_SERVICE_UUID key:KEY_SERVICE_UUID dict:configDict];
    
    [Config _save:[NSNumber numberWithLong:DEFAULT_SCAN_DURATION] key:KEY_SCAN_DURATION dict:configDict];
    [Config _save:[NSNumber numberWithLong:DEFAULT_SCAN_INTERVAL] key:KEY_SCAN_INTERVAL dict:configDict];
    
    [Config _save:[NSNumber numberWithLong:DEFAULT_ADVERTISE_INTERVAL] key:KEY_ADVERTISE_INTERVAL dict:configDict];
    [Config _save:[NSNumber numberWithLong:DEFAULT_ADVERTISE_DURATION] key:KEY_ADVERTISE_DURATION dict:configDict];
}

+(NSDictionary *)GetConfig {
    NSMutableDictionary* ans = [[NSMutableDictionary alloc] init];
    
    NSString* serviceUUID = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_SERVICE_UUID];
    ans[KEY_SERVICE_UUID] = serviceUUID;
    
    NSString* scanDuration = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_SCAN_DURATION];
    ans[KEY_SCAN_DURATION] = scanDuration;
    
    NSString* scanInterval = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_SCAN_INTERVAL];
    ans[KEY_SCAN_INTERVAL] = scanInterval;

    NSString* advertiseInterval = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_ADVERTISE_INTERVAL];
    ans[KEY_ADVERTISE_INTERVAL] = advertiseInterval;
    
    NSString* advertiseDuration = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_ADVERTISE_DURATION];
    ans[KEY_ADVERTISE_DURATION] = advertiseDuration;
    
    return ans;
}

+(void)_save:(id)defaultValue key:(NSString*)key dict:(NSDictionary*)dict {
    id element = [dict valueForKey:key] ?: defaultValue;
    
    @try {
        [[NSUserDefaults standardUserDefaults] setObject:element forKey:key];
    } @catch (NSError* error) {
        NSLog(@"ERROR %@", error);
    }
}

@end
