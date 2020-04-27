//
//  BLEManager.m
//  BLETest
//
//  Created by Ran Greenberg on 07/04/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//
#import "SpecialBleManager.h"
#import "rn_contact_tracing-Swift.h"


NSString *const EVENTS_FOUND_DEVICE         = @"foundDevice";
NSString *const EVENTS_SCAN_STATUS          = @"scanningStatus";
NSString *const EVENTS_ADVERTISE_STATUS     = @"advertisingStatus";

@interface SpecialBleManager ()

@property (nonatomic, strong) CBCentralManager* cbManager;
@property (nonatomic, strong) CBPeripheralManager* cbPeripheral;
@property (nonatomic, strong) CBService* service;
@property (nonatomic, strong) CBCharacteristic* characteristic;
@property (nonatomic, strong) RCTEventEmitter* eventEmitter;
@property (nonatomic, strong) NSString* scanUUIDString;
@property (nonatomic, strong) NSString* advertiseUUIDString;
@property (nonatomic, strong) NSString* publicKey;


@end

@implementation SpecialBleManager


#pragma mark - static methods

+ (id)sharedManager {
    static SpecialBleManager *sharedMyManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedMyManager = [[self alloc] init];
    });
    return sharedMyManager;
}

#pragma mark - public methods

- (instancetype)init {
    if (self = [super init]) {
        self.cbManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
        self.cbPeripheral = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
    }
    return self;
}

-(void)scan:(NSString *)serviceUUIDString withEventEmitter:(RCTEventEmitter*)emitter {
    if (serviceUUIDString == nil) {
        NSLog(@"Can't scan service when uuid is nil!");
        return;
    }
    self.eventEmitter = emitter;
    self.scanUUIDString = serviceUUIDString;
    CBUUID* UUID = [CBUUID UUIDWithString:serviceUUIDString];
    if (self.cbManager.state == CBManagerStatePoweredOn) {
        NSLog(@"Start scanning for %@", UUID);
        [self.cbManager scanForPeripheralsWithServices:@[UUID] options:nil];
        [self.eventEmitter sendEventWithName:EVENTS_SCAN_STATUS body:[NSNumber numberWithBool:YES]];
    }
}

- (void)stopScan:(RCTEventEmitter*)emitter {
    [self.cbManager stopScan];
    [self.eventEmitter sendEventWithName:EVENTS_SCAN_STATUS body:[NSNumber numberWithBool:NO]];
    self.scanUUIDString = nil;
}

-(void)advertise:(NSString *)serviceUUIDString publicKey:(NSString*)publicKey withEventEmitter:(RCTEventEmitter*)emitter {
    self.eventEmitter = emitter;
    self.advertiseUUIDString = serviceUUIDString;
    self.publicKey = [NSString stringWithFormat:@"%@-%@", [[UIDevice currentDevice] name], publicKey];
    if (self.cbPeripheral.state != CBManagerStatePoweredOn) {
        return;
    }
    if (self.service && self.characteristic) {
        [self _advertise];
    } else {
        [self _setServiceAndCharacteristics:serviceUUIDString publicKey:publicKey];
    }
}

- (void)stopAdvertise:(RCTEventEmitter*)emitter {
    [self.cbPeripheral stopAdvertising];
    [self.eventEmitter sendEventWithName:EVENTS_ADVERTISE_STATUS body:[NSNumber numberWithBool:NO]];
    self.advertiseUUIDString = nil;
}


#pragma mark - private methods

-(void) _setServiceAndCharacteristics:(NSString*)serviceUUIDString publicKey:(NSString*)publicKey {
    if (serviceUUIDString == nil) {
        return;
    }
    CBUUID* UUID = [CBUUID UUIDWithString:serviceUUIDString];
    CBMutableCharacteristic* myCharacteristic = [[CBMutableCharacteristic alloc]
                                                 initWithType:UUID
                                                 properties:CBCharacteristicPropertyRead
                                                 value:[publicKey dataUsingEncoding:NSUTF8StringEncoding]
                                                 permissions:0];
    CBMutableService* myService = [[CBMutableService alloc] initWithType:UUID primary:YES];
    myService.characteristics = [NSArray arrayWithObject:myCharacteristic];
    self.service = myService;
    self.publicKey = publicKey;
    self.characteristic = myCharacteristic;
    [self.cbPeripheral addService:myService];
}

-(void) _advertise {
    if (self.cbPeripheral.state == CBManagerStatePoweredOn){
        
        [self.cbPeripheral startAdvertising:@{CBAdvertisementDataLocalNameKey: self.publicKey, CBAdvertisementDataServiceUUIDsKey: @[self.service.UUID]}];
        [self.eventEmitter sendEventWithName:EVENTS_ADVERTISE_STATUS body:[NSNumber numberWithBool:YES]];
    }
}

#pragma mark - CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    //    NSLog(@"Central manager state: %d", central.state);
    //    [self scan:self.scanUUIDString withEventEmitter:self.eventEmitter];
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
    NSString* name = @"";
    NSString* public_key = @"";
    NSNumber* device_first_timestamp = @0;
    int tx = 0;
    
    NSLog(@"Discovered device with name: %@", peripheral.name);
    if (peripheral && peripheral.name != nil) {
        name = peripheral.name;
    }
    
    if (advertisementData && advertisementData[CBAdvertisementDataServiceDataKey] && advertisementData[CBAdvertisementDataServiceUUIDsKey]) {
        NSDictionary *dataService = advertisementData[CBAdvertisementDataServiceDataKey];
        CBUUID *serviceUUID = advertisementData[CBAdvertisementDataServiceUUIDsKey][0];
        
        NSData *data = dataService[serviceUUID];

        NSString *addressFromData = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        public_key = addressFromData;
    } else if (advertisementData && advertisementData[CBAdvertisementDataLocalNameKey]) {
        public_key = advertisementData[CBAdvertisementDataLocalNameKey];
    }
    
    if (advertisementData && advertisementData[@"kCBAdvDataTimestamp"]) {
        device_first_timestamp = advertisementData[@"kCBAdvDataTimestamp"];
    }
    
    if (advertisementData && advertisementData[CBAdvertisementDataTxPowerLevelKey]) {
        tx = [[NSString stringWithFormat:@"%@", advertisementData[CBAdvertisementDataTxPowerLevelKey]] intValue];
    }
    
    int rssiInt = [[NSString stringWithFormat:@"%@", RSSI] intValue];
    NSDictionary* device = @{
        @"public_key": public_key,
        @"device_rssi": [NSNumber numberWithInt:rssiInt],
        @"device_first_timestamp": device_first_timestamp,
        @"device_last_timestamp": device_first_timestamp,
        @"device_tx": [NSNumber numberWithInt:tx]
    };
    
    [self.eventEmitter sendEventWithName:EVENTS_FOUND_DEVICE body:device];
    
    [DBClient addDevice:device];
}

#pragma mark - CBPeripheralDelegate

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    NSLog(@"Peripheral manager atate: %d", peripheral.state);
    [self advertise:self.advertiseUUIDString publicKey:self.publicKey withEventEmitter:self.eventEmitter];
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
    NSLog(@"didStartAdvertising");
}

- (void)peripheralDidUpdateName:(CBPeripheral *)peripheral {
    NSLog(@"Peripheral name:%@", peripheral.name);
}
@end
