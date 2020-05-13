//
//  Config.m
//  rn-contact-tracing
//
//  Created by Ran Greenberg on 23/04/2020.
//

#import "Config.h"



@implementation Config

+(void)SetConfig:(NSDictionary*)configDict {
    [Config _save:DEFAULT_SERVICE_UUID key:KEY_SERVICE_UUID dict:configDict];
    
    [Config _save:DEFAULT_TOKEN key:KEY_TOKEN dict:configDict];
    
    [Config _save:[NSNumber numberWithLong:DEFAULT_SCAN_DURATION] key:KEY_SCAN_DURATION dict:configDict];
    [Config _save:[NSNumber numberWithLong:DEFAULT_SCAN_INTERVAL] key:KEY_SCAN_INTERVAL dict:configDict];
    
    [Config _save:[NSNumber numberWithLong:DEFAULT_ADVERTISE_INTERVAL] key:KEY_ADVERTISE_INTERVAL dict:configDict];
    [Config _save:[NSNumber numberWithLong:DEFAULT_ADVERTISE_DURATION] key:KEY_ADVERTISE_DURATION dict:configDict];
}

+(NSDictionary *)GetConfig {
    NSMutableDictionary* ans = [[NSMutableDictionary alloc] init];
    
    NSString* serviceUUID = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_SERVICE_UUID];
    ans[KEY_SERVICE_UUID] = serviceUUID ?: DEFAULT_SERVICE_UUID;
    
    NSNumber* scanDuration = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_SCAN_DURATION];
    ans[KEY_SCAN_DURATION] = scanDuration ?: @(DEFAULT_SCAN_DURATION);
    
    NSNumber* scanInterval = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_SCAN_INTERVAL];
    ans[KEY_SCAN_INTERVAL] = scanInterval ?: @(DEFAULT_SCAN_INTERVAL);

    NSNumber* advertiseInterval = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_ADVERTISE_INTERVAL];
    ans[KEY_ADVERTISE_INTERVAL] = advertiseInterval ?: @(DEFAULT_ADVERTISE_INTERVAL);
    
    NSNumber* advertiseDuration = [[NSUserDefaults standardUserDefaults] objectForKey:KEY_ADVERTISE_DURATION];
    ans[KEY_ADVERTISE_DURATION] = advertiseDuration ?: @(DEFAULT_ADVERTISE_DURATION);
    
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
