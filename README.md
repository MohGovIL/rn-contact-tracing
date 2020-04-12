# RN-Contact-Tracing 

## WIP Library - Don't Use in Production

## About
This is a React-Native library for tracing close contact between 2 devices.
We do this by advertising & scanning messages, over BLE (Bluetooth Low Energy), with the same ServiceUUID.
The library can do the following:
1. Advertise over BLE a messages with specific ServiceUUID and predefined PublicKey 
2. Scan for for a specific ServiceUUID BLE signals and store the scanned info into local device storage
   - timestamp, other-device-PubKey,RSSI (signal strength)
3. On Android these operations can be done in background 
4. On iOS - ???

##### Why did we build this lib?
TBD

##### Limitations
TBD

### How to run the example project
* `cd example`
* `npm install`
* `cd ios`
* `pod install`
* `npm run android` or `npm run ios`


## Getting started

`yarn add rn-contact-tracing`

or

`npm install rn-contact-tracing --save`


## Supported Platforms
* iOS 10+
* Android API 21+

### Android

### iOS

## Methods
* [`startBLEScan`](#startBLEScan)
* [`stopBLEScan`](#stopBLEScan)
* [`advertise`](#advertise)
* [`stopAdvertise`](#stopAdvertise)
* [`startBLEService`](#startBLEService)
* [`stopBLEService`](#stopBLEService)
* [`getAllDevices`](#getAllDevices)
* [`cleanDevicesDB`](#cleanDevicesDB)
* [`setPublicKeys`](#setPublicKeys)

## Events to JS
- `scanningStatus` - event can be true/false
- `advertisingStatus` - event can be  true/false
- `foundDevice` - event has 2 params: {event.device_name, event.device_address}


#### `startBLEScan(serviceUUID)`
Starts BLE scanning for a specific serviceUUID
- `serviceUUID` - the serviceUUID to scan for

#### `stopBLEScan()`
Stop BLE scanning

#### `advertise(serviceUUID)`
Advertise BLE device with a specific serviceUUID
- `serviceUUID` - serviceUUID to advertise with

#### `startBLEService(serviceUUID, pubKey, interval)`
start background service with a repeating scan & advertise with
- `serviceUUID` - the serviceUUID to advertise and to scan for
- `interval` - time interval in miliseconds, that we use to repeatadly execute advertise task   

#### `stopBLEService()`
Stops the background service and all the tasks the service executing

### References
* https://github.com/opentrace-community
* ....


