![CI](https://github.com/wix-incubator/rn-contact-tracing/workflows/CI/badge.svg)
# RN-Contact-Tracing 

## WIP Library - Don't Use in Production


## About
This is a react-native library for tracing close contact between 2 mobile devices by exchanging tokens over BLE (Bluetooth Low Energy).  

The library eventually will do the following:

1. Advertise messages with specific _service_uuid_ and generated _tokken_
2. Scan for for BLE signals with a specific _service_uuid_ and store the relevant data into local device storage
3. Provide simple API for JS to init these tasks in background and retrieve the scanned _tokkens_

This is temporary implementation until Google & Apple will release the full [Contact Tracing API](https://www.apple.com/covid19/contacttracing/) solution.

##### Why did we build this lib?
Due to COVID-19 pandemic, several groups and health authorities released apps that will help to identify and notify people that are at risk of exposure.

Some of these apps are written with RN and based on tracking user location which is not enough such as [Hamagen](https://github.com/MohGovIL/hamagen-react-native), and they willing to add BLE based functionality.  

There are lots of great libs that expose ble functionality for RN, i.e [react-native-ble-plx](https://github.com/Polidea/react-native-ble-plx) & [react-native-ble-manager](https://github.com/innoveit/react-native-ble-manager) but we wanted reduce the amount of dependancies as much as possible and exectue very specfic BLE functionality in background.

In addition, we looked at several great apps written for the same purpose in native, but each one of them is not written in a way that we could use as a stand-alone library. 
[OpenTrace](https://github.com/opentrace-community)) - includes the full business logic (UI..) that we don't want to use. 
[DP^3T Project](https://github.com/DP-3T) - include cryptography logic that we prefer to replace

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
npm install
cd example/ios
pod install
For Android - npm run android 
For iOS - npm run ios
``` 
In Android - Click on Request Location Permission button

### Installation
`yarn add rn-contact-tracing`

or

`npm install rn-contact-tracing --save`

### Supported Platforms
* iOS 10+
* Android API 21+



## Methods

### Summary

* [`setConfig`](#setConfig)
* [`getConfig`](#getConfig)
* [`startBLEService`](#startBLEService)
* [`stopBLEService`](#stopBLEService)
* [`startBLEScan`](#startBLEScan)
* [`stopBLEScan`](#stopBLEScan)
* [`startBLEAdvertise`](#startBLEScan)
* [`stopBLEAdvertise`](#stopBLEScan)


---


### Details

#### `setConfig(config)`

```javascript
   SpecialBle.setConfig(config);
```

Sets configuration options that will be used in scanning & advertising tasks.

**Parameters:**

| Name   | Type   | Required | Description |
| ------ | ------ | -------- | ----------- |
| config | object | Yes      | See below.  |

Supported options:

* `serviceUUID` - the ServiceUUID which identify the BLE broadcast you are going to advertise and scan for.
* `scanDuration` -  scanning duration in milisec
* `scanInterval` - the time in milisec between every scan
* `advertiseDuration` - advertising duration in milisec (up to 180000ms)
* `advertiseInterval` - the time in milisec between every advertising

For Android
* `advertiseTXPowerLevel` - advertise TX power level [docs](https://developer.android.com/reference/android/bluetooth/le/AdvertiseSettings.Builder#setTxPowerLevel(int))
* `scanMatchMode` - match mode for Bluetooth LE scan filters hardware match [docs](https://developer.android.com/reference/android/bluetooth/le/ScanSettings.Builder#setMatchMode(int))

---


#### `config getConfig()`

```javascript
   SpecialBle.getConfig((config) => {
    ....
   })
```

Gets the scanning & advertising configuration options that are currently defined in the library

---

#### `startBLEService(config)`

```javascript
   SpecialBle.startBLEService(config);
```

Starts BLE background task scanning for a specific  - config is optional

---

#### `stopBLEService()`

```javascript
   SpecialBle.stopBLEService();
```

Stops the background service and all the tasks the service executing

---

#### `startBLEScan(config)`

```javascript
   SpecialBle.startBLEScan(config);
```

Starts BLE scanning - config is optional

---

#### `stopBLEScan(config)`

```javascript
   SpecialBle.stopBLEScan();
```

Starts BLE scanning 

---






## Methods

* [`stopBLEScan`](#stopBLEScan)
* [`advertise`](#advertise)
* [`stopAdvertise`](#stopAdvertise)

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




### References
* https://github.com/opentrace-community
* ....


