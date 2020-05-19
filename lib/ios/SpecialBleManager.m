//
//  BLEManager.m
//  BLETest
//
//  Created by Ran Greenberg on 07/04/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//
#import "SpecialBleManager.h"
#import "rn_contact_tracing-Swift.h"
#import "Config.h"

NSString *const EVENTS_FOUND_DEVICE         = @"foundDevice";
NSString *const EVENTS_FOUND_SCAN           = @"foundScan";
NSString *const EVENTS_SCAN_STATUS          = @"scanningStatus";
NSString *const EVENTS_ADVERTISE_STATUS     = @"advertisingStatus";
// [[UIDevice currentDevice] name];

@interface SpecialBleManager ()

@property (nonatomic, strong) CBCentralManager* cbCentral;
@property (nonatomic, strong) CBPeripheralManager* cbPeripheral;
@property (nonatomic, strong) CBService* service;
@property (nonatomic, strong) CBCharacteristic* characteristic;
@property (nonatomic, strong) RCTEventEmitter* eventEmitter;
@property (nonatomic, strong) NSString* scanUUIDString;
@property (nonatomic, strong) NSString* advertiseUUIDString;
@property (nonatomic, strong) NSString* publicKey;

@property NSDictionary* config;
@property BOOL advertisingIsOn;
@property BOOL scanningIsOn;

@end

@implementation SpecialBleManager


#pragma mark - LifeCycle

+ (id)sharedManager {
    static SpecialBleManager *sharedMyManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedMyManager = [[self alloc] init];
    });
    return sharedMyManager;
}

- (instancetype)init {
    if (self = [super init]) {
        self.config = [Config GetConfig];
//        self.cbCentral = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
//        self.cbPeripheral = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
    }
    return self;
}

#pragma mark - public methods

#pragma mark BLE Services

- (void)startBLEServicesWithEventEmitter:(RCTEventEmitter*)emitter
{
    self.config = [Config GetConfig];
    
    // advertising state flag
    self.advertisingIsOn = YES;
    self.scanningIsOn = YES;
//    // add contact to DB
//    NSArray* eph = @[@1, @2, @3, @4, @5, @6, @7, @8, @9, @1, @2, @3, @4, @5, @6, @7];
//    NSArray* geo = @[@0, @0, @0, @0, @0];
//    [DBClient addContact:eph :11 :123456789 :geo :1];
//
    
    // set singleton's data
    self.publicKey = [CryptoClient getEphemeralId];
    self.eventEmitter = emitter;
    self.scanUUIDString = self.config[KEY_SERVICE_UUID] ;
    self.advertiseUUIDString = self.config[KEY_SERVICE_UUID];
    
    // init and start the scan Central
    if (!self.cbCentral)
        self.cbCentral = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    else
        [self scan:self.scanUUIDString withEventEmitter:emitter];
    
    // init and start the advertise Peropheral
    if (!self.cbPeripheral)
        self.cbPeripheral = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
    else
        [self advertise:self.advertiseUUIDString publicKey:self.publicKey withEventEmitter:emitter];
}

- (void)stopBLEServicesWithEmitter:(RCTEventEmitter*)emitter
{
    self.advertisingIsOn = NO;
    self.scanningIsOn = NO;
    [self stopScan:emitter];
    [self stopAdvertise:emitter];
}

#pragma mark Scan tasks

-(void)scan:(NSString *)serviceUUIDString withEventEmitter:(RCTEventEmitter*)emitter {
    if (serviceUUIDString == nil) {
        NSLog(@"Can't scan service when uuid is nil!");
        return;
    }
    if (self.cbCentral.state != CBManagerStatePoweredOn) {
        NSLog(@"Central service is off");
        return;
    }
    
    self.eventEmitter = emitter;
    self.scanUUIDString = serviceUUIDString;
    CBUUID* UUID = [CBUUID UUIDWithString:serviceUUIDString];
    
    // Note: *******
    /*
     We're using scan without durations and intervals since if we go to background when scanning is off the the interval task will not start when in background and scanning will be off until the application returns to foreground. When scan is linear and not turning off there is still chance to receive scans in the backgroung although by apple's documentation when in background, the scan rate will slow down dramatically and CBCentralManagerScanOptionAllowDuplicatesKey is ignored (each perfipheral should be found only once when in BG)
     */
    
    // *********** scan linear witout duration / interval ********** //
    NSLog(@"Start scanning for %@", UUID);
    [self.cbCentral scanForPeripheralsWithServices:@[UUID] options:nil];
    [self.eventEmitter sendEventWithName:EVENTS_SCAN_STATUS body:[NSNumber numberWithBool:YES]];
    // ******** end of scan interval ********** //
    
    // **** scnning with intervals and duration ****** //
//    NSLog(@"Start scanning for %@, duration:%d , interval:%d", UUID,
//    [self.config[KEY_SCAN_DURATION] intValue]/1000, [self.config[KEY_SCAN_INTERVAL] intValue]/1000 );
//    if (self.scanningIsOn)
//    {
//        [self.cbCentral scanForPeripheralsWithServices:@[UUID] options:nil];
//        [self.eventEmitter sendEventWithName:EVENTS_SCAN_STATUS body:[NSNumber numberWithBool:YES]];
//        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(([self.config[KEY_SCAN_DURATION] intValue] / 1000) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//            [self stopScan:self.eventEmitter];
//            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(([self.config[KEY_SCAN_INTERVAL] intValue] / 1000) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//                [self scan:self.scanUUIDString withEventEmitter:self.eventEmitter];
//            });
//        });
//    }
//    else
//        NSLog(@"interval received but advertising is off!!!");
    // ******** end of scan with duration ********** //
}

- (void)stopScan:(RCTEventEmitter*)emitter {
    [self.cbCentral stopScan];
    [self.eventEmitter sendEventWithName:EVENTS_SCAN_STATUS body:[NSNumber numberWithBool:NO]];
//    self.scanUUIDString = nil;
}

#pragma mark Advertise tasks

-(void)advertise:(NSString *)serviceUUIDString publicKey:(NSString*)publicKey withEventEmitter:(RCTEventEmitter*)emitter {
    if (self.cbPeripheral.state != CBManagerStatePoweredOn) {
        return;
    }
    self.eventEmitter = emitter;
    self.advertiseUUIDString = serviceUUIDString;
    self.publicKey = [CryptoClient getEphemeralId];
    if (self.service && self.characteristic) {
        [self _advertise];
    } else {
        [self _setServiceAndCharacteristics:serviceUUIDString];
    }
}

- (void)stopAdvertise:(RCTEventEmitter*)emitter {
    [self.cbPeripheral stopAdvertising];
    [self.eventEmitter sendEventWithName:EVENTS_ADVERTISE_STATUS body:[NSNumber numberWithBool:NO]];
//    self.advertiseUUIDString = nil;
}

#pragma mark - private methods

-(void) _setServiceAndCharacteristics:(NSString*)serviceUUIDString {
    if (serviceUUIDString == nil) {
        return;
    }
    CBUUID* UUID = [CBUUID UUIDWithString:serviceUUIDString];
    CBMutableCharacteristic* myCharacteristic = [[CBMutableCharacteristic alloc]
                                                 initWithType:UUID
                                                 properties:CBCharacteristicPropertyRead
                                                 value:[self.publicKey dataUsingEncoding:NSUTF8StringEncoding]
                                                 permissions:0];
    CBMutableService* myService = [[CBMutableService alloc] initWithType:UUID primary:YES];
    myService.characteristics = [NSArray arrayWithObject:myCharacteristic];
    self.service = myService;
    self.characteristic = myCharacteristic;
    [self.cbPeripheral addService:myService];
}

-(void) _advertise {
    if (self.cbPeripheral.state == CBManagerStatePoweredOn){
        self.publicKey = [CryptoClient getEphemeralId];
        [self.cbPeripheral startAdvertising:@{CBAdvertisementDataLocalNameKey: self.publicKey, CBAdvertisementDataServiceUUIDsKey: @[self.service.UUID]}];
        [self.eventEmitter sendEventWithName:EVENTS_ADVERTISE_STATUS body:[NSNumber numberWithBool:YES]];
    }
}

#pragma mark - CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    switch (central.state) {
            case CBManagerStateUnknown:
                NSLog(@"cntral.state is Unknown");
                break;
            case CBManagerStateResetting:
                NSLog(@"cntral.state is Resseting");
                break;
            case CBManagerStateUnsupported:
                NSLog(@"cntral.state is Unsupported");
                break;
            case CBManagerStateUnauthorized:
                NSLog(@"cntral.state is Unauthorized");
                break;
            case CBManagerStatePoweredOff:
                NSLog(@"cntral.state is Powered off");
                break;
            case CBManagerStatePoweredOn:
                NSLog(@"cntral.state is Powered on");
                [self scan:self.scanUUIDString withEventEmitter:self.eventEmitter];
                break;
            default:
                break;
        }
}

- (void)centralManager:(CBCentralManager *)central
 didDiscoverPeripheral:(CBPeripheral *)peripheral
     advertisementData:(NSDictionary<NSString *,id> *)advertisementData
                  RSSI:(NSNumber *)RSSI {
    NSString* public_key = @"";
    NSNumber *tx = @0;
    int64_t unixtime = [[NSDate date] timeIntervalSince1970]*1000;

    if (peripheral && peripheral.name != nil) {
        NSLog(@"Discovered device with name: %@", peripheral.name);
    }
    
    // get private_key
    if (advertisementData && advertisementData[CBAdvertisementDataServiceDataKey] && advertisementData[CBAdvertisementDataServiceUUIDsKey]) {
        // Androids device...
        NSDictionary *dataService = advertisementData[CBAdvertisementDataServiceDataKey];
        CBUUID *serviceUUID = advertisementData[CBAdvertisementDataServiceUUIDsKey][0];
        
        NSData *data = dataService[serviceUUID];

        if (data.length == 16) {
            public_key = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding] ?: @"";
            [CryptoClient printDecodedKey:public_key];
        } else {
            public_key = @"";
        }
    } else if (advertisementData && advertisementData[CBAdvertisementDataLocalNameKey]) {
        // IOS device...
        public_key = advertisementData[CBAdvertisementDataLocalNameKey];
        if (public_key.length == 16)
        {
            [CryptoClient printDecodedKey:public_key];
        }
        else
            public_key = @"";
    } else {
        NSLog(@"UNKnown device");
        NSLog(@"*** empty publicKey received");
        if (advertisementData)
            NSLog(@"AdvertisementData: %@", advertisementData);
    }
        
    
    if (public_key.length == 0)
    {
        return;
    }
    // get TX
    if (advertisementData && advertisementData[CBAdvertisementDataTxPowerLevelKey]) {
        tx = advertisementData[CBAdvertisementDataTxPowerLevelKey];
    }
    
    // add contact to DB
    NSArray* geo = @[@0, @0, @0, @0, @0];

    [DBClient addContactWithAsciiEphemeral:public_key :[RSSI integerValue] :unixtime :geo];
    
    // get current device from DB
    NSArray* devicesArray = [DBClient getDeviceByKey:public_key];

    NSMutableDictionary* device;
    if (devicesArray.count == 0)
    { // a new device found, add to DB
        device = [NSMutableDictionary dictionaryWithDictionary:@{
            @"public_key": public_key,
            @"device_rssi": RSSI,
            @"device_first_timestamp": @(unixtime),
            @"device_last_timestamp": @(unixtime),
            @"device_tx": tx,
            @"device_address": @"", //TODO: not used may remove
            @"device_protocol": @"GAP" //TODO: not used may remove
        }];
        [DBClient addDevice:device];

    }
    else
    { // old device found, just update
        device = [NSMutableDictionary dictionaryWithDictionary:[devicesArray firstObject]];
        // update device
        [device setValue:@(unixtime) forKey:@"device_last_timestamp"];
        [device setValue:RSSI forKey:@"device_rssi"];
        [DBClient updateDevice:device];
    }

    // send foundDevice event
    [self.eventEmitter sendEventWithName:EVENTS_FOUND_DEVICE body:device];

    // handle scans
    NSArray* scansArray = [DBClient getScanByKey:public_key];
    NSDictionary* scan = @{
        @"scan_id": @(scansArray.count),
        @"public_key": public_key,
        @"scan_rssi": RSSI,
        @"scan_timestamp": @(unixtime),
        @"scan_tx": tx,
        @"scan_address": @"", //TODO: not used maybe remove
        @"scan_protocol": @"GAP" //TODO: not used maybe remove
    };

    // add to DB
    [DBClient addScan:scan];

    // send foundScan event
    [self.eventEmitter sendEventWithName:EVENTS_FOUND_SCAN body:scan];
}

#pragma mark - CBPeripheralDelegate

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    switch (peripheral.state) {
        case CBManagerStateUnknown:
                NSLog(@"Peripheral.state is Unknown");
                break;
            case CBManagerStateResetting:
                NSLog(@"Peripheral.state is Resseting");
                break;
            case CBManagerStateUnsupported:
                NSLog(@"Peripheral.state is Unsupported");
                break;
            case CBManagerStateUnauthorized:
                NSLog(@"Peripheral.state is Unauthorized");
                break;
            case CBManagerStatePoweredOff:
                NSLog(@"Peripheral.state is Powered off");
                break;
            case CBManagerStatePoweredOn:
                NSLog(@"Peripheral.state is Powered on");
                NSLog(@"publicKey: %@",self.publicKey);
                if (self.publicKey)
                    [self advertise:self.advertiseUUIDString publicKey:self.publicKey withEventEmitter:self.eventEmitter];
                else {
                    self.publicKey = [CryptoClient getEphemeralId];
                    [self advertise:self.advertiseUUIDString publicKey:self.publicKey withEventEmitter:self.eventEmitter];
                }
                break;
            default:
                break;
    }
}

- (void)peripheralManager:(CBPeripheralManager *)peripheral
            didAddService:(CBService *)service
                    error:(NSError *)error {
    if (error) {
        NSLog(@"Error publishing service: %@", [error localizedDescription]);
    } else {
        NSLog(@"Service added with UUID:%@", service.UUID);
        [self _advertise];
    }
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral
                                       error:(NSError *)error {
    if (error) {
        NSLog(@"didStartAdvertising: Error: %@", error);
        return;
    }
    NSLog(@"didStartAdvertising, duration:%d , interval:%d",
    [self.config[KEY_ADVERTISE_DURATION] intValue]/1000, [self.config[KEY_ADVERTISE_INTERVAL] intValue]/1000 );
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(([self.config[KEY_ADVERTISE_DURATION] intValue] / 1000) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self stopAdvertise:self.eventEmitter];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(([self.config[KEY_ADVERTISE_INTERVAL] intValue] / 1000) * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (self.advertisingIsOn)
                [self _advertise];
            else
                NSLog(@"interval received but advertising is off!!!");
        });
    });
}

- (void)peripheralDidUpdateName:(CBPeripheral *)peripheral {
    NSLog(@"Peripheral name:%@", peripheral.name);
}

- (NSString*)fetchInfectionData
{
    return @"";
}

-(NSString*)findMatchForInfections:(NSString*)jsonString
{
    NSData *data;
    if (jsonString.length > 0)
    {
        data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    }
    else // TODO: only to tests!!! getting hardCoded file
    {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"outputserverReponse" ofType:@"json"];
        if (!path)
        {
            return @"file not found";
        }
        data = [NSData dataWithContentsOfFile:path];
    }
    NSError* error;
    NSDictionary* matchDict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
    
    if (error)
    {
        NSLog(@"Error parsing JSON: %@",error);
        return @"Error parsing JSON";
    }
    NSString* resJSON = [CryptoClient findMatch:[matchDict[@"startDay"] integerValue] :matchDict[@"infected"]];
    NSLog(@"%@",resJSON);
    return resJSON;
}


- (void) writeContactsDB:(NSString*)jsonString
{
    NSData *data;
    if (jsonString.length > 0)
    {
        data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    }
    else // TODO: only to tests!!! getting hardCoded file
    {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"outputcontacts" ofType:@"json"];
        if (!path)
        {
            NSLog(@"file not found");
            return;
        }
        
        data = [NSData dataWithContentsOfFile:path];
    }
    NSError* error;
    NSArray<NSDictionary*>* contactsArray = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
    
    if (!error)
    {
        int numberOfContactsAdded = 0;
        for (NSDictionary* contactDict in contactsArray) {
            [DBClient addJsonContact:contactDict[@"ephemeral_id"] :[contactDict[@"rssi"] integerValue] :[contactDict[@"timestamp"] integerValue] : contactDict[@"geohash"]];
            numberOfContactsAdded+=1;
        }
        NSLog(@"%d",numberOfContactsAdded);
    }
    else
    {
        NSLog(@"cannot parse json DB file: %@", error);
    }
}

@end
