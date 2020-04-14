# RN-Contact-Tracing 

## WIP Library - Don't Use in Production

## About
This is a react-native library for tracing close contact between 2 mobile devices by exchanging tokens over BLE (Bluetooth Low Energy).
The library eventually will do the following:

1. Advertise messages with specific _service_uuid_ and predefined _public_key_ 
2. Scan for for BLE signals with a specific _service_uuid_  store the scanned data into local device storage
   - _timestamp_, _other_public_key_ ,_rssi_ (signal strength)
3. Provide simple API for JS to init these tasks in background and retrieve the scanned _public_key_ 

This is temporary implementation until Google & Apple will release the full [Contact Tracing API](https://www.apple.com/covid19/contacttracing/)

##### Why did we build this lib?
Due to COVID-19 pandemic, several health authorities released apps that will help to identify and notify people that are at risk of exposure.  
Some of these apps are written with RN and based on tracking user location which is not enough such as [Hamagen](https://github.com/MohGovIL/hamagen-react-native), so they willing to add BLE based functionality.  
There are lots of great libs that implement ble native-module for RN, i.e [react-native-ble-plx](https://github.com/Polidea/react-native-ble-plx) & [react-native-ble-manager](https://github.com/innoveit/react-native-ble-manager) but we wanted something very basic:
* Run BLE functionality & execute some basic business logic in background (generate keys, decide what save to DB)
* Reduce the amount of dependencies    

In addition, there are several great apps written for the same purpose in native ([COVID19CZ](https://github.com/covid19cz), [OpenTrace](https://github.com/opentrace-community)),but they includes the full business logic (UI..) that we don't want to use. 
  
  
##### Privacy (what do we advertise and save to DB)
TBD

 
##### Limitations
TBD

## Working plan

 Functionality | Andorid | iOS | WIP
:------------ | :-------------| :-------------| :-------------|
Scan in foreground | :white_check_mark: |  :white_check_mark: | |
Advertise in foreground | :white_check_mark: |  :white_check_mark: | |
Scan in background | :white_check_mark: | TODO | |
Advertise in background | :white_check_mark: | TODO | |
Save scanned data into local DB | :white_check_mark: | TODO | |
Return scanned data to JS | :white_check_mark: | TODO | 
Pass scannng & advertising configuration from JS (intervals..) | TODO | TODO | | 
Receive _public_keys_ from JS  |TODO|TODO| |
Generate _public_keys_ from the native code  |TODO|TODO| |
Deal with permissions |TODO|TODO| |
Tests  |TODO|TODO| |
Features for rssi calibration  |TODO|TODO|Taboola|


## Getting started

### How to run the example project
```properties
cd example
npm install
cd ios
pod install
For Android - npm run android 
For iOS - npm run ios
``` 
In Android - approve location permission manually

### Installation
`yarn add rn-contact-tracing`

or

`npm install rn-contact-tracing --save`

### Supported Platforms
* iOS 10+
* Android API 21+


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


#### Events from Native to JS
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


