# rn-special-ble


## Requirements

## Supported Platforms
* iOS X+
* Android API X+

## Install

### Android

### iOS

## API

#### startBLEScan(serviceUUID)
Starts BLE scanning for a specific serviceUUID
- `serviceUUID` - the serviceUUID to scan for

#### stopBLEScan()
Stop BLE scanning

#### advertise(serviceUUID)
Advertise BLE device with a specific serviceUUID
- `serviceUUID` - serviceUUID to advertise with

startBLEService(serviceUUID)
#### startBLEService(serviceUUID, interval)
start background service with a repeating scan & advertise with
- `serviceUUID` - the serviceUUID to advertise and to scan for
- `interval` - time interval in miliseconds, that we use to repeatadly execute advertise task   

Stop BLE scanning
#### stopBLEService()
Stops the background service and all the tasks the service executing


## Events to JS
- `scanningStatus` - event can be true/false
- `advertisingStatus` - event can be  true/false
- `foundDevice` - event has 2 params: {event.device_name, event.device_address}
