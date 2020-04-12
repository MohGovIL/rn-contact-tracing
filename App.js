/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {useEffect, useState} from 'react';
import {
    Button,
    NativeEventEmitter,
    NativeModules,
    TouchableOpacity,
    Text,
    FlatList,
    View,
    StyleSheet,
} from 'react-native';

const serviceUUID = '00000000-0000-1000-8000-00805F9B34FB';
const BLEModule = NativeModules.BLEModule;

const App: () => React$Node = () => {


    const [scanningStatus, setScanningStatus] = useState(false);
    const [advertisingStatus, setAdvertisingStatus] = useState(false);

    const [devices, setDevices] = useState([{device_name: 'NaN', device_address: 'NaN'}]);

    useEffect(() => {
        const eventEmitter = new NativeEventEmitter(NativeModules.BLEModule);
        eventEmitter.addListener('scanningStatus', (status) => setScanningStatus(status));
        eventEmitter.addListener('advertisingStatus', (status) => setAdvertisingStatus(status));
        eventEmitter.addListener('serviceStatus', (status) => setServiceStatus(status));
        eventEmitter.addListener('foundDevices', (event) => {
                if (!devices.some(device => {device.device_address === event.device_address})) {
                    setDevices([...devices, {device_name: event.device_name, device_address: event.device_address}]);
                }
            },
        );
    }, []);


    function _startScan() {
        BLEModule.startBLEScan(serviceUUID);
    }

    function _stoptScan() {
        BLEModule.stopBLEScan();
    }

    function _advertise() {
        BLEModule.advertise(serviceUUID);
    }

    function _startBLEService() {
        BLEModule.startBLEService(serviceUUID);
    }

    function _stopBLEService() {
        BLEModule.stopBLEService();
    }

    return (
        <View style={styles.container}>

            <Text>Scanning: {scanningStatus.toString()} </Text>
            <Text>Advertising: {advertisingStatus.toString()}</Text>

            <TouchableOpacity style={styles.btn} onPress={_startScan}>
                <Text>Start Scan</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.btn} onPress={_stoptScan}>
                <Text>Stop Scan</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.btn} onPress={_advertise}>
                <Text>Start Advertise</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.btn} onPress={_startBLEService}>
                <Text>Start BLE service</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.btn} onPress={_stopBLEService}>
                <Text>Stop BLE service</Text>
            </TouchableOpacity>

            <FlatList
                data={devices}
                style={{marginTop: 5}}
                keyExtractor={item => item.device_address}
                renderItem={({item}) => <Text style={styles.item}>{item.device_name} : {item.device_address} </Text>}
            />
        </View>
    );
};


const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingTop: 20,
        marginHorizontal: 16,
    },
    btn: {
        margin: 10,
        padding: 10,
        alignItems: 'center',
        backgroundColor: 'orange',
    },
    item: {
        padding: 10,
        fontSize: 18,
        height: 44,
    },
});

export default App;
