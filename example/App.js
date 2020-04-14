import React, {useEffect, useState} from 'react';
import {
  NativeEventEmitter,
  TouchableOpacity,
  Text,
  FlatList,
  View,
  StyleSheet,
  PermissionsAndroid,
  Platform
} from 'react-native';
import SpecialBle from 'rn-contact-tracing';

const SERVICE_UUID = '00000000-0000-1000-8000-00805F9B34FB';
const PUBLIC_KEY = '12345678901234567';
const TAG = "EXAMPLE";


const App: () => React$Node = () => {

  const [scanningStatus, setScanningStatus] = useState(false);
  const [advertisingStatus, setAdvertisingStatus] = useState(false);
  const [devices, setDevices] = useState([]);

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(SpecialBle);
    eventEmitter.addListener('scanningStatus', (status) => setScanningStatus(status));
    eventEmitter.addListener('advertisingStatus', (status) => setAdvertisingStatus(status));
    eventEmitter.addListener('foundDevice', (event) => {
      console.log(event);
      _getAllDevicesFromDB();
      },
    );
    _getAllDevicesFromDB();
  }, []);



  // Start scanning for a specific serviceUUID
  function _startScan() {
    SpecialBle.startBLEScan(SERVICE_UUID);
  }

  // Stop scanning
  function _stoptScan() {
    SpecialBle.stopBLEScan();
  }

  // Start advertising with SERVICE_UUID & PUBLIC_KEY
  function _advertise() {
    SpecialBle.advertise(SERVICE_UUID,PUBLIC_KEY);
  }

  // Stop advertising
  function _stopAdvertise() {
    SpecialBle.stopAdvertise();
  }

  // in Android - start foreground service with scanning & advertising tasks
  function _startBLEService() {
    SpecialBle.startBLEService(SERVICE_UUID,PUBLIC_KEY);
  }

  // stop background tasks
  function _stopBLEService() {
    SpecialBle.stopBLEService();
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
    let publicKeys = ['12345','12346','12347','12348','12349']
    SpecialBle.setPublicKeys(publicKeys);
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
                <Text>Scanning: {scanningStatus.toString()} </Text>
                <Text>Advertising: {advertisingStatus.toString()}</Text>
                {_renderPermissionButton()}
            </View>
            <View style={styles.subContainer}>
                {_renderButton('Start Scan', _startScan)}
                {_renderButton('Stop Scan', _stoptScan)}
            </View>
            <View style={styles.subContainer}>
                {_renderButton('Start Advertise', _advertise)}
                {_renderButton('Stop Advertise', _stopAdvertise)}
            </View>
            <View style={styles.subContainer}>
                {_renderButton('Start BLE service', _startBLEService)}
                {_renderButton('Stop BLE service', _stopBLEService)}
                {_renderButton('Get all devices from DB', _getAllDevicesFromDB)}
                {_renderButton('Remove Devices from DB', _cleanAllDevicesFromDB)}
            </View>
            <View style={styles.subContainer}>
                {_renderButton('Set public Keys', _setPublicKeys)}
            </View>

            <FlatList
                data={devices}
                style={{marginTop: 5}}
                keyExtractor={item => item.public_key}
                renderItem={({item}) => <Text style={styles.item}>
                    {item.public_key} :
                    {item.device_address} :
                    {item.device_rssi} </Text>}
            />
        </View>
    );

    function _renderButton(text, onClick) {
            return (
                <TouchableOpacity style={styles.btn} onPress={onClick}>
                    <Text>{text}</Text>
                </TouchableOpacity>
            )
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
    flex: 1,
    paddingTop: 100,
    marginHorizontal: 5,
  },
  subContainer: {
    flexWrap: 'wrap',
    flexDirection: 'row',
    alignItems: 'center'
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

export default App;
