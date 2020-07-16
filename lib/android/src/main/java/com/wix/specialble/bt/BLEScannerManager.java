package com.wix.specialble.bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Base64;
import android.util.Log;
import androidx.core.app.ActivityCompat;

import com.wix.crypto.Contact;
import com.wix.crypto.CryptoManager;
import com.wix.crypto.utilities.BytesUtils;
import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.listeners.IEventListener;
import com.wix.specialble.sensor.AccelerometerManager;
import com.wix.specialble.sensor.ProximityManager;
import com.wix.specialble.sensor.RotationVectorManager;
import com.wix.specialble.sensor.SensorUtils;
import com.wix.specialble.util.Constants;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BLEScannerManager {

    private static final String PRIMARY_SERVICE_UUID = "00000000-0000-1000-8000-00805F9B34FB";
    private static final String KEEP_ALIVE_UUID = "00000000-0000-1000-8000-00805F9B34FA";

    private static final String SCANNING_STATUS = "scanningStatus";
    private static final String FOUND_DEVICE = "foundDevice";
    private static final String FOUND_SCAN = "foundScan";
    private RotationVectorManager mRotationVectorManager;
    private AccelerometerManager mAccelerometerManager;
    private ProximityManager mProximityManager;

    Context mContext;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    SpecialBLEScanCallback bleScanCallback;
    DBClient dbClient;

    private String TAG = "BLEScannerManager";
    private IEventListener mEventListenerCallback;

    private BluetoothGattService gattService;
    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic primary;
    private BluetoothGattCharacteristic keepAlive;

    private static int contactsConstraint = 19;
    private static int timeConstraint = 60 * 1000;
    private Map<String, List<Contact>> ephemeralsToSendMap;
    private static final byte[] END_OFF_GATT_READ_RESPONSE = "no_more_contacts".getBytes();
    List<Contact> filteredContactListToSend;

    // this is a place holder for Geo-Hash Data
    // it should be injected from the application container level on every change from LocationManager
    // TODO: provide external API to the react native level
    ///////////////////////////////////////////////////////////////
    public static byte[] sGeoHash = new byte[]{0, 0, 0, 0, 0};

    BLEScannerManager(Context context, IEventListener eventListenerCallback) {
        mContext = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bleScanCallback = new SpecialBLEScanCallback();
        dbClient = DBClient.getInstance(context);
        mEventListenerCallback = eventListenerCallback;

        // declare sensors
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mProximityManager = new ProximityManager(sensorManager);
        mAccelerometerManager = new AccelerometerManager(sensorManager);
        mRotationVectorManager = new RotationVectorManager(sensorManager);

        gattService = new BluetoothGattService(UUID.fromString(PRIMARY_SERVICE_UUID),BluetoothGattService.SERVICE_TYPE_PRIMARY);
        primary = new BluetoothGattCharacteristic(UUID.fromString(PRIMARY_SERVICE_UUID), BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        keepAlive = new BluetoothGattCharacteristic(UUID.fromString(KEEP_ALIVE_UUID), BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE );

        gattService.addCharacteristic(primary);
        gattService.addCharacteristic(keepAlive);

        gattServer = ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).openGattServer(context, bluetoothGattServerCallback);
        gattServer.addService(gattService);

        ephemeralsToSendMap = new HashMap<>();
    }

    public void startScan(String serviceUUID) {
        if (bluetoothAdapter.isEnabled()) {

            if(bluetoothLeScanner == null) // if we turned the bluetooth on while the service is running
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            Config config = Config.getInstance(mContext);
            int scanMatchMode = config.getScanMatchMode();

            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID))).build();
            ScanFilter nullFilter = new ScanFilter.Builder().setServiceUuid(null).setManufacturerData(76, Base64.decode("AQAAAAAAAAAAAEAAAAAAAAA=",Base64.DEFAULT)).build(); // .setManufacturerData(76, Base64.decode("AQAAAAAAAAAAAEAAAAAAAAA=",Base64.DEFAULT))

            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);
            filters.add(nullFilter);

            ScanSettings settings = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                settings = new ScanSettings.Builder().setMatchMode(scanMatchMode).build();
            } else {
                settings = new ScanSettings.Builder().build();
            }

            if(bluetoothLeScanner != null)
                bluetoothLeScanner.startScan(filters, settings, bleScanCallback);
            mEventListenerCallback.onEvent(SCANNING_STATUS, true);
        }
    }

    public void stopScan() {
        if(bluetoothAdapter.isEnabled()){

            if(bluetoothLeScanner == null) // if we turned the bluetooth on while the service is running
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            if(bluetoothLeScanner != null)
                bluetoothLeScanner.stopScan(bleScanCallback);

            mEventListenerCallback.onEvent(SCANNING_STATUS, false);
        }
    }

    class SpecialBLEScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) {
                Log.e(TAG, "onScanResult: scanRecord is null!");
                return;
            }

            if (scanRecord.getServiceUuids() == null) {
                Log.e(TAG, "onScanResult: getServiceUuids is null!");
                return;
            }

            ParcelUuid pUuid = scanRecord.getServiceUuids().get(0);
            byte[] byteScannedToken = result.getScanRecord().getServiceData(pUuid);

            String deviceName = scanRecord.getDeviceName() != null ? scanRecord.getDeviceName() : "NaN";
            String ScannedToken = byteScannedToken != null ? new String(byteScannedToken, Charset.forName("UTF-8")) : deviceName;

            int tx = scanRecord.getTxPowerLevel();

            super.onScanResult(callbackType, result);
            handleScanResults(result, ScannedToken, tx, byteScannedToken);
        }

        @Override
        public void onScanFailed(final int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode != SCAN_FAILED_ALREADY_STARTED) {
                mEventListenerCallback.onEvent(SCANNING_STATUS, false);
            }
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DBClient.getInstance(mContext).insert(new Event(System.currentTimeMillis(), "none", Constants.ACTION_SCAN, "failure", String.valueOf(errorCode), SensorUtils.getBatteryPercentage(mContext)));
                }
            });
        }
    }

    private void handleScanResults(final ScanResult result, final String scannedToken, final int tx, final byte[] byteScannedToken) {
        // handle devices
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DBClient.getInstance(mContext).insert(new Event(System.currentTimeMillis(), scannedToken, Constants.ACTION_SCAN, "success", "", SensorUtils.getBatteryPercentage(mContext)));

                Device oldDevice = dbClient.getDeviceByKey(scannedToken); // get device from database
                Device newDevice;

                if (oldDevice != null) {
                    newDevice = getNewDevice(result, tx, scannedToken, oldDevice.getFirstTimestamp(), System.currentTimeMillis());
                    if (hasUpdateRequirements(oldDevice, result)) {
                        dbClient.updateDevice(newDevice);
                    }
                } else {
                    newDevice = getNewDevice(result, tx, scannedToken, System.currentTimeMillis(), System.currentTimeMillis());
                    dbClient.addDevice(newDevice);
                }

                mEventListenerCallback.onEvent(FOUND_DEVICE, newDevice.toWritableMap());

                // handle scans
                Scan newScan = new Scan(System.currentTimeMillis(),
                        scannedToken,
                        result.getDevice().getAddress(),
                        BLEManager.BLEProtocol.GAP.toString(),
                        result.getRssi(),
                        tx, mProximityManager.getEvents()[0], mAccelerometerManager.getEvents(),
                        mRotationVectorManager.getEvents(), SensorUtils.getBatteryPercentage(mContext));

                dbClient.addScan(newScan);

                int currentTime = (int) (System.currentTimeMillis() / 1000);

                byte[] rssi = BytesUtils.numToBytes(result.getRssi(), 4);

                double lat = 0;
                double lon = 0;
                try {
                    LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {

                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        } else {


                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }

                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //TODO:: do we need to make sure our key is exactly 16 byte len as per the spec ?
                CryptoManager.getInstance(mContext).mySelf.storeContact(byteScannedToken, rssi, currentTime, sGeoHash, lat, lon, -1);

                mEventListenerCallback.onEvent(FOUND_SCAN, newScan.toWritableMap());
            }
        });
    }

    private boolean hasUpdateRequirements(Device oldDevice, ScanResult result) {
        return (oldDevice.getRssi()>result.getRssi()+3 || oldDevice.getRssi()<result.getRssi()-3);
    }

    private Device getNewDevice(ScanResult result, int tx, String scannedToken, long firstSeenTime, long lastSeenTime) {
        return new Device(firstSeenTime, lastSeenTime, scannedToken, result.getDevice().getAddress(),
                BLEManager.BLEProtocol.GAP.toString(), result.getRssi(), tx);
    }

    private Device getNewDevice(int rssi, BluetoothDevice device, int tx, String scannedToken, long firstSeenTime, long lastSeenTime) {
        return new Device(firstSeenTime, lastSeenTime, scannedToken, device.getAddress(),
                BLEManager.BLEProtocol.GAP.toString(), rssi, tx);
    }

    private void registerSensors() {
        mProximityManager.registerListener();
        mAccelerometerManager.registerListener();
        mRotationVectorManager.registerListener();
    }

    private void unregisterSensors() {
        mProximityManager.unregisterListener();
        mAccelerometerManager.unregisterListener();
        mRotationVectorManager.unregisterListener();
    }

    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {

                Log.e(TAG, "BluetoothGattServerCallback#onConnectionStateChange: STATE_CONNECTED");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e(TAG, "BluetoothGattServerCallback#onConnectionStateChange: STATE_DISCONNECTED ");
                ephemeralsToSendMap.remove(device.getAddress());
                gattServer.cancelConnection(device);
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.e(TAG, "BluetoothGattServerCallback#onServiceAdded: status " + status + " service uuid " + service.getUuid().toString());
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicReadRequest: for device " + device.getAddress());
//            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, "success".getBytes());

            byte[] fullResponse = new byte[0];
            for (int i = 0; i < filteredContactListToSend.size(); i++) {
                if (i == contactsConstraint) {
                    break;
                }
                Contact contactToSend = filteredContactListToSend.get(i);
                byte[] curerntContact = BytesUtils.byteConcatenation(contactToSend.getEphemeral_id(), String.valueOf(contactToSend.getTimestamp()).getBytes());
                fullResponse = BytesUtils.byteConcatenation(fullResponse, curerntContact);
            }
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, fullResponse);

        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, final byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicWriteRequest: value is " + new String(value) + " for device " + device.getAddress());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    Device oldDevice = dbClient.getDeviceByKey(new String(value)); // get device from database
                    Device newDevice;

                    if (oldDevice != null) {
                        newDevice = getNewDevice(0,device,0,new String(value), oldDevice.getFirstTimestamp(), System.currentTimeMillis());
//                        if (hasUpdateRequirements(oldDevice, result)) {
                            dbClient.updateDevice(newDevice);
//                        }
                    } else {
                        newDevice = getNewDevice(0,device,0,new String(value), System.currentTimeMillis(), System.currentTimeMillis());
                        dbClient.addDevice(newDevice);
                    }

                    Scan newScan = new Scan(System.currentTimeMillis(),
                            new String(value),
                            device.getAddress(),
                            BLEManager.BLEProtocol.GAP.toString(),
                            0,
                            0, mProximityManager.getEvents()[0], mAccelerometerManager.getEvents(),
                            mRotationVectorManager.getEvents(), SensorUtils.getBatteryPercentage(mContext));

                    dbClient.addScan(newScan);

                    int currentTime = (int) (System.currentTimeMillis() / 1000);

                    byte[] rssi = BytesUtils.numToBytes(0, 4);

                    double lat = 0;
                    double lon = 0;

                    CryptoManager.getInstance(mContext).mySelf.storeContact(value, rssi, currentTime, sGeoHash, lat, lon, System.currentTimeMillis());

                    List<Contact> contacts = DBClient.getInstance(mContext).getAllContactsWithGattServerConnection(timeConstraint);
                    Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicWriteRequest: fetched  " + contacts.size() + " from database" );

                    Map<String, Contact> filteredMap = new HashMap<>();
                    for(Contact contact : contacts) {
                        if(!filteredMap.containsKey(new String(contact.getEphemeral_id()))) {
                            filteredMap.put(new String(contact.getEphemeral_id()), contact);
                        }
                    }

                    filteredContactListToSend = new ArrayList<>(filteredMap.values());
                }
            });

            if(responseNeeded) {
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, ("success write").getBytes());
            }
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.e(TAG, "BluetoothGattServerCallback#onExecuteWrite: " );
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.e(TAG, "BluetoothGattServerCallback#onNotificationSent: status " + status);
        }

    };
}

// BluetoothGattCallback
// BluetoothGattServerCallback