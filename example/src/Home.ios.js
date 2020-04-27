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
    SafeAreaView
} from 'react-native';
import SpecialBle from 'rn-contact-tracing';
import {Button, Badge, Colors, Divider, View, TextField} from 'react-native-ui-lib';
const SERVICE_UUID = '00000000-0000-1000-8000-00805F9B34FB';
const PUBLIC_KEY = 'IOS-1234';
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
  const [devices, setDevices] = useState([]);
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
        _getConfig()
    }, []);


    // Start scanning for a specific serviceUUID
    function _startScan() {
        SpecialBle.setConfig(config)
        if (Platform.OS === 'android')
        {
          SpecialBle.startBLEScan();
        }          
        else
        {
          SpecialBle.startBLEScan(SERVICE_UUID);
        }      
    }

    // Stop scanning
    function _stoptScan() {
        SpecialBle.stopBLEScan();
    }

    // Start advertising with SERVICE_UUID & PUBLIC_KEY
    function _startAdvertise() {
        SpecialBle.setConfig(config);
        if (Platform.OS === 'android')
        {
          SpecialBle.advertise();
        }
        else
        {
          SpecialBle.advertise(SERVICE_UUID,PUBLIC_KEY);
        }
    }

    // Stop advertising
    function _stopAdvertise() {
        SpecialBle.stopAdvertise();
    }

    // in Android - start foreground service with scanning & advertising tasks
    function _startBLEService() {
        SpecialBle.setConfig(config);
        if (Platform.OS === 'android')
        {
          SpecialBle.startBLEService();
        }
        else
        {
          SpecialBle.startBLEService(SERVICE_UUID,PUBLIC_KEY);
        }
    }

    // stop background tasks
    function _stopBLEService() {
        SpecialBle.stopBLEService();
    }

    // get all devices from DB
    async function _getAllDevicesFromDB() {
        if (Platform.OS === 'android')
        {
          SpecialBle.getAllDevices((devices) => {
              setDevices(devices)
          })
        }
        else
        {
          SpecialBle.getAllDevices((err, devices) => {
            setDevices(devices)
          })
        }
    }

    // clean all devices from DB
    function _cleanAllDevicesFromDB() {
        SpecialBle.cleanDevicesDB();
        _getAllDevicesFromDB();
    }

    // add demo device
    function _scanDemoDevice() {
      if (Platform.OS === 'ios')
        SpecialBle.addDemoDevice();
    }

    // add list of public_keys
    function _setPublicKeys() {
        let publicKeys = ['12345', '12346', '12347', '12348', '12349']
        SpecialBle.setPublicKeys(publicKeys);
        alert(config.scanInterval)
    }

  // get config
  function _getConfig() {
    if (Platform.OS === 'ios')
    {
      SpecialBle.getConfig((config) => {
        alert(JSON.stringify(config));
      })
    }
    else
    {
      setConfig(config);
    }
  }

  // set config
  function _setConfig() {
    if (Platform.OS === 'ios')
    {
      SpecialBle.setConfig({
        serviceUUID: '',
        scanDuration: 0,
        scanInterval: 0,
        advertiseInterval: 0,
        advertiseDuration: 0
      })
    }
  }

    // request location permission (only for Android)
    async function _requestLocationPermission() {
        try {
            const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION
            );
            if (granted === PermissionsAndroid.RESULTS.GRANTED) {
                alert("Location Permission Granted");
            } else {
                alert("Location Permission Denied");
            }
        } catch (err) {
            console.warn(err);
        }
    }


    return (
        <View style={styles.container}>
            
                <View style={styles.subContainer}>
                    {_statusBadge('Scanning', scanningStatus.toString() === 'true')}
                    {_statusBadge('Advertising', advertisingStatus.toString() === 'true')}
                    {_renderPermissionButton()}
                </View>
                <Text text80BL>ServiceUUID: {SERVICE_UUID}</Text>

                <View style={styles.subContainer}>
                        {_renderButton('Start Scan', _startScan)}
                        {_renderButton('Start Advertise', _startAdvertise)}
                        
                </View>
            
            <View style={styles.subContainer}>
                {_renderButton('Stop Scan', _stoptScan)}
                {_renderButton('Stop Advertise', _stopAdvertise)}
            </View>

            
          <ScrollView>
                {_renderTextField("Advertised Token", config.token, val => setConfig({
                    ...config,
                    token: val
                }))}

                <Text style={{fontSize: 20, fontWeight: 'bold', marginVertical: 10}}>Scan</Text>
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
          <SafeAreaView style={{flex: 1}}>
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
          </SafeAreaView>

                <Text style={{fontSize: 20, fontWeight: 'bold', marginVertical: 10}}>Advertise</Text>
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
                
                <SafeAreaView style={{flex: 1}}>
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
                </SafeAreaView>

                <Text text80BL style={{marginHorizontal: 10}}>Advertise TX Power</Text>

                <SafeAreaView style={{flex: 1}}>
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
                </SafeAreaView>

            <View style={styles.subContainer}>
                {_renderButton('Start BLE service', _startBLEService)}
                {_renderButton('Stop BLE service', _stopBLEService)}
            </View>
            
            
                
            
            <View style={styles.subContainer}>
                {_renderButton('Set public Keys', _setPublicKeys)}
            </View>
            
            <View style={styles.subContainer}>
              {_renderButton('Get Config', _getConfig)}
              {_renderButton('Set Config', _setConfig)}
            </View>
            
            <View style={[styles.subContainer, {display : Platform.OS === 'android' ? 'none' : 'flex'}]}>
                {_renderButton('Get all devices from DB', _getAllDevicesFromDB)}
                {_renderButton('Remove Devices from DB', _cleanAllDevicesFromDB)}
                {_renderButton('Demo Scan Device', _scanDemoDevice)}
            </View>
            
            <SafeAreaView style={{flex: 1}}>
              <FlatList
                  data={devices}
                  style={{marginTop: 5}}
                  keyExtractor={item => item.public_key}
                  renderItem={({item}) => <Text style={styles.item}>
                      {item.public_key} :
                      {item.device_address} :
                      {item.device_rssi} </Text>}
              />
            </SafeAreaView>
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


    function _renderTextField(placeHolder, value, onChangeText, keyboardType= "default") {
        return (
            <TextField
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

    function _renderPermissionButton() {
        if (Platform.OS === 'android')
            return (
                _renderButton('Location Permission', _requestLocationPermission)
            );
        return null;
    }
};


const styles = StyleSheet.create({

    container: {
        marginTop: 20,
        flex: 1,
        marginHorizontal: 5,
    },
    statusContainer: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center'
    },
    subContainer: {
        flexWrap: 'wrap',
        flexDirection: 'row',
        alignItems: 'center'
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
    btn: {
      marginHorizontal: 5,
      marginVertical:10,
      padding: 10,
      alignItems: 'center',
      backgroundColor: 'orange'
    },
    item: {
        padding: 10,
        height: 44,
    },
});

export default HomeScreen;
