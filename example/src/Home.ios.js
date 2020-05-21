import React, {useEffect, useState} from 'react';
import _ from 'lodash';
import {
    NativeEventEmitter,
    TouchableOpacity,
    Text,
    FlatList,
    StyleSheet,
    PermissionsAndroid,
    Platform,
    Picker,
    ScrollView,
    TextInput
} from 'react-native';
import SpecialBle from 'rn-contact-tracing';
import {Button, Badge, Colors, View} from 'react-native-ui-lib';

const SERVICE_UUID = '00000000-0000-1000-8000-00805F9B34FB';
const PUBLIC_KEY = 'IOS-1234';
const TAG = "EXAMPLE";


function HomeScreen() {
    const [scanningStatus, setScanningStatus] = useState(false);
    const [advertisingStatus, setAdvertisingStatus] = useState(false);
    const [devices, setDevices] = useState([]);
    // const config = {
    //     serviceUUID: SERVICE_UUID,
    //     scanDuration: 50000,
    //     scanInterval: 10000,
    //     advertiseInterval: 45000,
    //     advertiseDuration: 10000,
    //     token: 'default_token'
    //   };
    const [config, setConfig] = useState({
        serviceUUID: '00000000-0000-1000-8000-00805F9B34FB',
        scanDuration: 60000,
        scanInterval: 240000,
        advertiseInterval: 45000,
        advertiseDuration: 10000,
        large_icon: 'notificationLargeIconPath',
        small_icon: 'notificationSmallIconPath',
        token: 'default_token'
    });

    useEffect(() => {
        const eventEmitter = new NativeEventEmitter(SpecialBle);
        eventEmitter.addListener('scanningStatus', (status) => setScanningStatus(status));
        eventEmitter.addListener('advertisingStatus', (status) => setAdvertisingStatus(status));
        _getConfig()
    }, []);


    // Start scanning for a specific serviceUUID
    function _startScan() {
        SpecialBle.setConfig(config)
        SpecialBle.startBLEScan(SERVICE_UUID);
    }

    // Stop scanning
    function _stoptScan() {
        SpecialBle.stopBLEScan();
    }

    // Start advertising with SERVICE_UUID & PUBLIC_KEY
    function _startAdvertise() {
        SpecialBle.setConfig(config);
        SpecialBle.advertise(SERVICE_UUID, PUBLIC_KEY);
    }

    // Stop advertising
    function _stopAdvertise() {
        SpecialBle.stopAdvertise();
    }

     // get all devices from DB
     async function _getAllDevicesFromDB() {
        SpecialBle.getAllDevices((err, devices) => {
            setDevices(devices)
        })
    }

    // clean all devices from DB
    function _cleanAllDevicesFromDB() {
        SpecialBle.cleanDevicesDB();
        _getAllDevicesFromDB();
    }

   // add list of public_keys
   function _setPublicKeys() {
        // let publicKeys = ['12345', '12346', '12347', '12348', '12349']
        // SpecialBle.setPublicKeys(publicKeys);
        //        alert(config.scanInterval)
    }

    // get config
    function _getConfig() {
        SpecialBle.getConfig((config) => {
            setConfig(config);
        })
    }

    // set config
    function _setConfig() {
        SpecialBle.setConfig(config)
    }

    // clean all scans from DB
    function _cleanAllScansFromDB() {
        SpecialBle.cleanScansDB();
        _getAllDevicesFromDB();
    }
    
    // add demo device
    function _scanDemoDevice() {
      if (Platform.OS === 'ios')
        SpecialBle.addDemoDevice();
    }

    /////IGATES API For App

    // in IOS - starts the ble scan and peripheral services, sets the uuid and public keys and starts the scanning & advertising tasks
    function _startBLEService() {
        SpecialBle.setConfig(config);
        SpecialBle.startBLEService();
    }

    // stop background tasks
    function _stopBLEService() {
        SpecialBle.stopBLEService();
    }

    // clean all devices from DB
    function _wipe() {
        SpecialBle.deleteDatabase();
    }

    // match
    function _match(infected_db) {
        SpecialBle.match("", (res) => {
            alert(res);
          });
    }
    // function _match(infected_db) {
    //     return SpecialBle.match("infected_db");

    //     // return [[0,1,12,255,1,1,1,1,1,1,1,1,1,1,1,1], [0,1,12,255,1,1,1,1,1,1,1,1,1,1,1,1]];
    // }

    // fetch
    function fetchInfectionDataByConsent() {
        // return SpecialBle.fetchInfectionDataByConsent();
        SpecialBle.fetchInfectionDataByConsent((res) => {
            alert(res);
        });
    }

    // add contacts to DB
    function _writeContactsToDB() {
        SpecialBle.writeContactsToDB(null);
    }

    return (
        <View style={styles.container}>
            <ScrollView>
            
            <View style={styles.subContainer}>
                {_statusBadge('Scanning', scanningStatus.toString() === 'true')}
                {_statusBadge('Advertising', advertisingStatus.toString() === 'true')}
            </View>
            <Text text80BL>ServiceUUID: {config.serviceUUID}</Text>
            <Text text80BL>  </Text>
            <View style={[styles.subContainer, {justifyContent: 'center'}]}>
                    {_renderButton('Start BLE service', _startBLEService)}
                    {_renderButton('Stop BLE service', _stopBLEService)}
            </View>

            <View style={[styles.subContainer, {justifyContent: 'center'}]}>
                {_renderButton('Wipe data', _wipe)}
                {_renderButton('Match infected', _match)}
            </View>

            <View style={[styles.subContainer, {justifyContent: 'center'}]}>

                {_renderButton('Fetch server infected keys', fetchInfectionDataByConsent)}
            </View>

            <View style={[styles.subContainer, {justifyContent: 'center'}]}>
                {_renderButton('Add Contacts to DB', _writeContactsToDB)}
            </View>

            <View style = {{display: 'none'}}>

                <View style={styles.subContainer}>
                    {_renderButton('Start Scan', _startScan)}
                    {_renderButton('Start Advertise', _startAdvertise)}

                </View>

                <View style={styles.subContainer}>
                    {_renderButton('Stop Scan', _stoptScan)}
                    {_renderButton('Stop Advertise', _stopAdvertise)}
                </View>
            </View>



            <View>
            
            <View style = {{display: 'none'}}>
                {_renderTextField("Advertised Token", config.token, val => setConfig({
                    ...config,
                    large_icon: 'notificationLargeIconPath',
                    small_icon: 'notificationSmallIconPath',
                    token: val
                }))}
            </View>
                <Text style={{fontSize: 20, fontWeight: 'bold', marginVertical: 10}}>Scan</Text>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    <Text style={{fontSize: 18, fontWeight: 'normal', marginVertical: 5}}>scanDuration</Text>
                    <Text style={{fontSize: 18, fontWeight: 'normal', marginVertical: 5}}>scanInterval</Text>
                </View>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    {_renderTextField("Duration in ms", config.scanDuration.toString(), val => setConfig({
                        ...config,
                        large_icon: 'notificationLargeIconPath',
                        small_icon: 'notificationSmallIconPath',
                        scanDuration: parseInt(val)
                    }), "numeric")}
                    {_renderTextField("Interval in ms", config.scanInterval.toString(), val => setConfig({
                        ...config,
                        large_icon: 'notificationLargeIconPath',
                        small_icon: 'notificationSmallIconPath',
                        scanInterval: parseInt(val)
                    }), "numeric")}
                </View>

                <Text style={{fontSize: 20, fontWeight: 'bold', marginVertical: 10}}>Advertise</Text>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    <Text style={{fontSize: 18, fontWeight: 'normal', marginVertical: 5}}>advertiseDuration</Text>
                    <Text style={{fontSize: 18, fontWeight: 'normal', marginVertical: 5}}>advertiseInterval</Text>
                </View>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    {_renderTextField("Duration in ms", config.advertiseDuration.toString(), val => setConfig({
                        ...config,
                        large_icon: 'notificationLargeIconPath',
                        small_icon: 'notificationSmallIconPath',
                        advertiseDuration: parseInt(val)
                    }), "numeric")}
                    {_renderTextField("Interval in ms", config.advertiseInterval.toString(), val => setConfig({
                        ...config,
                        large_icon: 'notificationLargeIconPath',
                        small_icon: 'notificationSmallIconPath',
                        advertiseInterval: parseInt(val)
                    }), "numeric")}
                </View>

                <Text text80BL>  </Text>
                <Text text80BL>Config: </Text>
                <Text text80BL>  </Text>

                <View style={[styles.subContainer, {justifyContent: 'center'}]}>
                    
                    {_renderButton('Get Config', _getConfig)}
                    {_renderButton('Set Config', _setConfig)}
                </View>
            </View>

            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>
            <Text text80BL>  </Text>

            <View style = {{display: 'none'}}>
                <View style={styles.subContainer}>
                    {_renderButton('Set public Keys', _setPublicKeys)}
                </View>

                

                <View style={[styles.subContainer, {display: Platform.OS === 'android' ? 'none' : 'flex'}]}>
                    {_renderButton('Get all devices from DB', _getAllDevicesFromDB)}
                    {_renderButton('Remove Devices from DB', _cleanAllDevicesFromDB)}
                    {_renderButton('Remove Scans from DB', _cleanAllScansFromDB)}
                    {_renderButton('Demo Scan Device', _scanDemoDevice)}
                </View>
            </View>
        </ScrollView>
    </View>

    );

    function _renderButton(text, onClick) {
        return (
            <Button
                backgroundColor={Colors.blue30}
                label={text}
                size='small'
                borderRadius={0}
                labelStyle={{fontWeight: '300'}}
                style={{marginBottom: 20, marginHorizontal: 10}}
                enableShadow
                onPress={onClick}
            />
        );
    }


    function _renderTextField(placeHolder, value, onChangeText, keyboardType = "default") {
        return (
            <TextInput
                style={{marginHorizontal: 10, width: 140}}
                floatingPlaceholder
                placeholder={placeHolder}
                floatOnFocus
                keyboardType={keyboardType}
                onChangeText={onChangeText}
                value={value}
            />
        );
    }


    function _statusBadge(statusName, isOn) {
        return (
            <View style={styles.statusContainer}>
                <Text>{statusName}</Text>
                <Badge
                    style={{marginHorizontal: 10}}
                    size="small"
                    backgroundColor={isOn == true ? Colors.green30 : Colors.red30}
                />
            </View>
        );
    }
};


const styles = StyleSheet.create({

    container: {
        marginTop: 20,
        flex: 1,
        marginHorizontal: 5,
        paddingLeft: 10
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginVertical: 10
    },
    statusContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10
    },
    subContainer: {
        flexDirection: 'row',
    },
    subContainerTextFields: {
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        flexDirection: 'row',
    },
    picker: {
        marginHorizontal: 10,
        width: 300
    },
    item: {
        padding: 10,
        height: 44,
    },
});

export default HomeScreen;
