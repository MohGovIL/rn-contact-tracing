import React, {Fragment, useEffect, useState} from 'react';
import _ from 'lodash';
import {
    NativeEventEmitter,
    TouchableOpacity,
    FlatList,
    StyleSheet,
    PermissionsAndroid,
    Platform,
    Picker,
    Text,
    ScrollView
} from 'react-native';
import SpecialBle, {requestLocationPermission, checktLocationPermission} from 'rn-contact-tracing';
import {Button, Badge, Colors, Divider, View, TextField} from 'react-native-ui-lib';

const SERVICE_UUID = '00000000-0000-1000-8000-00805F9B34FB';
const BATTERY_LEVEL_THRESHOLD = 5;
const TAG = "EXAMPLE";

const ScanMatchMode = [
    {value: 1, label: 'MATCH_MODE_AGGRESSIVE'},
    {value: 2, label: 'SCAN_MODE_LOW_LATENCY'},
];

const AdvertiseMode = [
    {value: 0, label: 'ADVERTISE_MODE_LOW_POWER'},
    {value: 1, label: 'ADVERTISE_MODE_BALANCED'},
    {value: 2, label: 'ADVERTISE_MODE_LOW_LATENCY'},
];

const AdvertiseTXPower = [
    {value: 0, label: 'ADVERTISE_TX_POWER_ULTRA_LOW'},
    {value: 1, label: 'ADVERTISE_TX_POWER_LOW'},
    {value: 2, label: 'ADVERTISE_TX_POWER_MEDIUM'},
    {value: 3, label: 'ADVERTISE_TX_POWER_HIGH'},
];


function HomeScreen() {
    const [scanningStatus, setScanningStatus] = useState(false);
    const [advertisingStatus, setAdvertisingStatus] = useState(false);
    const [permissions, setPermissions] = useState({location: false, ignoreBatteryOpt: false});
    const [config, setConfig] = useState({
        serviceUUID: '',
        scanDuration: 0,
        scanInterval: 0,
        advertiseInterval: 0,
        advertiseDuration: 0,
        advertiseMode: 0,
        token: 'default_token'
    });

    useEffect(() => {
        const eventEmitter = new NativeEventEmitter(SpecialBle);
        eventEmitter.addListener('scanningStatus', (status) => setScanningStatus(status));
        eventEmitter.addListener('advertisingStatus', (status) => setAdvertisingStatus(status));
        eventEmitter.addListener('checkPermission', () => _checkPermissions());
        _getConfig();
        _checkPermissions();
    }, []);


    async function _checkPermissions() {
        let isGranted = await checktLocationPermission();
        let ignoreBatteryOpt = await SpecialBle.isBatteryOptimizationDeactivated();
        setPermissions({...permissions, ...{location: isGranted, ignoreBatteryOpt: ignoreBatteryOpt}})
    }


    // Start scanning for a specific serviceUUID
    function _startScan() {
        SpecialBle.setConfig(config)
        SpecialBle.startBLEScan();
    }

    // Stop scanning
    function _stoptScan() {
        SpecialBle.stopBLEScan();
    }

    // Start advertising with SERVICE_UUID & PUBLIC_KEY
    function _startAdvertise() {
        SpecialBle.setConfig(config);
        SpecialBle.advertise();
    }

    // Stop advertising
    function _stopAdvertise() {
        SpecialBle.stopAdvertise();
    }


    // get all devices from DB
    async function _getAllDevicesFromDB() {
        SpecialBle.getAllDevices((devices) => {
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
        let publicKeys = ['12345', '12346', '12347', '12348', '12349']
        SpecialBle.setPublicKeys(publicKeys);
        alert(config.scanInterval)
    }

    // get Config
    function _getConfig() {
        SpecialBle.getConfig((config) => {
            setConfig({
                ...config,
                notificationLargeIconPath: 'large_icon.png',
                notificationSmallIconPath: 'small_icon',
                batteryLevel: BATTERY_LEVEL_THRESHOLD
            })
        })
    }

    /////IGATES API For App

    // in Android - start foreground service with scanning & advertising tasks
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

    // get match results
    function _match() {
        SpecialBle.match(null, result => { return result; });
    }

    // get user contacts data
    function fetchInfectionDataByConsent() {
        SpecialBle.fetchInfectionDataByConsent( result => {return result;});
    }

  

    function _renderBatteryOptimizedQueryButton() {
        if (Platform.OS === 'android')
            return (
                _renderButton('Is Optimized?', _requestIsBatteryOptimized)
            );
        return null;
    }


    // request to disable battery optimization (only for Android >= API 23)
    function _requestToDisableBatteryOptimization() {
        SpecialBle.requestToDisableBatteryOptimization();
    }

    return (
        <View style={styles.container}>
            <ScrollView>
                {_renderMainSection()}
                {_renderPermissionSection()}
                {_renderScanSection()}
                {_renderAdvertiseSection()}
            </ScrollView>
        </View>
    );


    function _renderMainSection() {
        return (
            <Fragment>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    {_statusBadge('Scanning', scanningStatus.toString() === 'true', {fontSize: 16, fontWeight: 'bold'})}
                    {_statusBadge('Advertising', advertisingStatus.toString() === 'true', {
                        fontSize: 16,
                        fontWeight: 'bold'
                    })}
                </View>

                <Text style={{fontSize: 14, marginVertical: 10}}>ServiceUUID: {config.serviceUUID}</Text>
                {_renderTextField("Advertised Token", config.token, val => setConfig({
                    ...config,
                    token: val
                }))}
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
 
                    {_renderButton('Load Database', _loadDatabase)}

                </View>

                <View style={[styles.subContainer, {justifyContent: 'center'}]}>
 
                    {_renderButton('Export contatcs', _exportContacts)}

                </View>

                <View style={[styles.subContainer, {justifyContent: 'center'}]}>

                    {_renderButton('Export Scans data', _exportScansData)}

                </View>

                <View style={[styles.subContainer, {justifyContent: 'center'}]}>

                    {_renderButton('Export Advertise data', _exportAdvertiseData)}

                </View>

            </Fragment>
        )
    }

    function _exportAdvertiseData() {
        SpecialBle.exportAdvertiseAsCSV();
    }

    function _exportScansData() {
        SpecialBle.exportScansDataAsCSV();
    }

    function _exportContacts() {

        SpecialBle.exportAllContactsAsCsv();
    }

    function _loadDatabase() {

        SpecialBle.writeContactsToDB(null);
    }

    function _renderPermissionSection() {
        return (
            <Fragment>
                <Text style={styles.sectionTitle}>Permissions</Text>

                <View style={[styles.subContainer, {justifyContent: 'space-around', marginVertical: 10}]}>
                    {_statusBadge('Location', permissions.location === true)}
                    {_statusBadge('Disabled Battery Opt', permissions.ignoreBatteryOpt === true)}
                </View>
                <View style={[styles.subContainer, {justifyContent: 'space-around'}]}>
                    {_renderButton('Ask Location', async () => {
                        setPermissions({...permissions, ...{location: await requestLocationPermission()}})
                    })}
                    {_renderButton('Ask Disable Battery Optimization', _requestToDisableBatteryOptimization)}
                </View>
            </Fragment>
        )
    }

    function _renderScanSection() {
        return (
            <View>
                <Text style={styles.sectionTitle}>Scan</Text>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    {_renderTextField("Duration in ms", config.scanDuration.toString(), val => setConfig({
                        ...config,
                        scanDuration: parseInt(val)
                    }), "numeric")}
                    {_renderTextField("Interval in ms", config.scanInterval.toString(), val => setConfig({
                        ...config,
                        scanInterval: parseInt(val)
                    }), "numeric")}
                </View>

                <Text text80BL style={{marginHorizontal: 10}}>Match Mode</Text>
                <Picker
                    style={styles.picker}
                    selectedValue={config.scanMatchMode}
                    onValueChange={(val) => {
                        setConfig({...config, scanMatchMode: val})
                    }}
                >
                    {_.map(ScanMatchMode, x => (
                        <Picker.Item key={x.value} value={x.value} label={x.label}/>
                    ))}
                </Picker>

                <View style={styles.subContainer}>
                    {_renderButton('Start Scan', _startScan)}
                    {_renderButton('Stop Scan', _stoptScan)}
                </View>
            </View>
        )
    }

    function _renderAdvertiseSection() {
        return (
            <View>
                <Text style={styles.sectionTitle}>Advertise</Text>
                <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                    {_renderTextField("Duration in ms", config.advertiseDuration.toString(), val => setConfig({
                        ...config,
                        advertiseDuration: parseInt(val)
                    }), "numeric")}
                    {_renderTextField("Interval in ms", config.advertiseInterval.toString(), val => setConfig({
                        ...config,
                        advertiseInterval: parseInt(val)
                    }), "numeric")}
                </View>

                <Text text80BL style={{marginHorizontal: 10}}>Advertise Mode</Text>
                <Picker
                    style={styles.picker}
                    selectedValue={config.advertiseMode}
                    onValueChange={(val) => {
                        setConfig({...config, advertiseMode: val})
                    }}
                >
                    {_.map(AdvertiseMode, x => (
                        <Picker.Item key={x.value} value={x.value} label={x.label}/>
                    ))}
                </Picker>

                <Text text80BL style={{marginHorizontal: 10}}>Advertise TX Power</Text>


                <Picker
                    style={styles.picker}
                    selectedValue={config.advertiseTXPowerLevel}
                    onValueChange={(val) => {
                        setConfig({...config, advertiseTXPowerLevel: val})
                    }}
                >
                    {_.map(AdvertiseTXPower, x => (
                        <Picker.Item key={x.value} value={x.value} label={x.label}/>
                    ))}
                </Picker>


                <View style={styles.subContainer}>
                    {_renderButton('Start Advertise', _startAdvertise)}
                    {_renderButton('Stop Advertise', _stopAdvertise)}
                </View>
            </View>
        )
    }

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
            <TextField
                style={{marginHorizontal: 5, marginBottom: 0, width: 140}}
                floatingPlaceholder
                placeholder={placeHolder}
                floatOnFocus
                keyboardType={keyboardType}
                onChangeText={onChangeText}
                value={value}
            />
        );
    }


    function _statusBadge(statusName, isOn, textStyle) {
        return (
            <View style={styles.statusContainer}>
                <Text style={textStyle}>{statusName}</Text>
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
