/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import "AppDelegate.h"

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTRootView.h>
#import <Firebase.h>

#import <rn-contact-tracing/SpecialBleManager.h>

@implementation AppDelegate
{
  BOOL didStartBle;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];
  RCTRootView *rootView = [[RCTRootView alloc] initWithBridge:bridge
                                                   moduleName:@"example"
                                            initialProperties:nil];

  rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  UIViewController *rootViewController = [UIViewController new];
  rootViewController.view = rootView;
  self.window.rootViewController = rootViewController;
  [self.window makeKeyAndVisible];
  if ([FIRApp defaultApp] == nil) {
    [FIRApp configure];    
  }
  
  // location
  if (self.locationManager == nil)
      self.locationManager = [[CLLocationManager alloc] init];
  self.locationManager.delegate = self;
  self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
  self.locationManager.allowsBackgroundLocationUpdates = YES;
  self.locationManager.distanceFilter = kCLDistanceFilterNone;
  self.locationManager.pausesLocationUpdatesAutomatically = NO;
  
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(bleStarted:)
                                               name:@"BLE_Started"
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(bleStoped:)
                                               name:@"BLE_Stoped"
                                             object:nil];
  
  return YES;
}

#pragma mark - NSNotification

-(void) bleStarted: (NSNotification*) notification
{
  [self.locationManager requestAlwaysAuthorization];
  [self.locationManager startUpdatingLocation];

  didStartBle = YES;
}

-(void) bleStoped: (NSNotification*) notification
{
  [self.locationManager stopUpdatingLocation];
}

#pragma mark - CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"didFailWithError: %@", error);
  
}

- (void)locationManager:(CLLocationManager *)manager
     didUpdateLocations:(NSArray *)locations
{
    NSLog(@"didUpdateLocation");
  
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];

  NSString* filepath = [[NSString alloc] init];
  NSError *err;

  filepath = [documentsDirectory stringByAppendingPathComponent:@"Location_logs.txt"];

  NSString *contents = [NSString stringWithContentsOfFile:filepath encoding:(NSStringEncoding)NSUnicodeStringEncoding error:nil] ?: @"";

  NSDate* UTCNow = [NSDate date];
  NSTimeZone *tz = [NSTimeZone defaultTimeZone];
  NSInteger seconds = [tz secondsFromGMTForDate: UTCNow];
  NSDate* now = [NSDate dateWithTimeInterval: seconds sinceDate: UTCNow];;

  NSString* text2log = [NSString stringWithFormat:@"%@\n%@ - %@",contents, now, @"updateLocation" ];
  BOOL ok = [text2log writeToFile:filepath atomically:YES encoding:NSUnicodeStringEncoding error:&err];
  
  if (!ok) {
      NSLog(@"Error writing file at %@\n%@",
      filepath, [err localizedFailureReason]);
  }
  
  [[SpecialBleManager sharedManager] keepAliveBLEStartForTask:@"Update Location"];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

@end
