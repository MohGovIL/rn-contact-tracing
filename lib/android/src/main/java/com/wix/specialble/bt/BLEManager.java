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
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.wix.crypto.Contact;
import com.wix.crypto.CryptoManager;
import com.wix.crypto.utilities.BytesUtils;
import com.wix.specialble.EventToJSDispatcher;
import com.wix.specialble.config.Config;
import com.wix.specialble.db.DBClient;
import com.wix.specialble.listeners.IEventListener;
import com.wix.specialble.sensor.SensorUtils;
import com.wix.specialble.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class BLEManager implements IEventListener {

    private static final String TAG = "BLEManager";

    private BluetoothAdapter bluetoothAdapter;
    Context context;
    private BLEScannerManager bleScanner;
    private BLEAdvertisingManager bleAdvertiser;
    private static BLEManager sBLEManagerInstance;
    private Config mConfig;
    private EventToJSDispatcher mEventToJSDispatcher;

    private static final String PRIMARY_SERVICE_UUID = "00000000-0000-1000-8000-00805F9B34FB";
    private static final String KEEP_ALIVE_UUID = "00000000-0000-1000-8000-00805F9B34FA";

    private static final int CONTACTS_FROM_DATABASE_CONSTRAINTS = 19;
    private static int CONTACTS_INSERTION_TIME_CONSTRAINTS = 60 * 1000; //60 secs in millis

    private BluetoothGattService gattService;
    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic primary;
    private BluetoothGattCharacteristic keepAlive;

    public static byte[] sGeoHash = new byte[]{0, 0, 0, 0, 0};

    private static final byte[] END_OFF_GATT_READ_RESPONSE = "no_more_contacts".getBytes();
    List<Contact> filteredContactListToSend;

    public enum BLEProtocol {
        GAP, GATT
    }

    private BLEManager(Context context) {
        this.context = context;
        init();
    }

    public static BLEManager getInstance(Context context) {
        if (sBLEManagerInstance == null) {
            sBLEManagerInstance = new BLEManager(context);
        }
        return sBLEManagerInstance;
    }

    public static BLEManager getInstance() throws Exception {
        if (sBLEManagerInstance == null) {
            throw new Exception("missing context");
        }
        return sBLEManagerInstance;
    }

    public void init() {
        mConfig = Config.getInstance(context);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bleScanner = new BLEScannerManager(context, this);
            bleAdvertiser = new BLEAdvertisingManager(context, this);

            gattService = new BluetoothGattService(UUID.fromString(PRIMARY_SERVICE_UUID),BluetoothGattService.SERVICE_TYPE_PRIMARY);
            primary = new BluetoothGattCharacteristic(UUID.fromString(PRIMARY_SERVICE_UUID), BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
            keepAlive = new BluetoothGattCharacteristic(UUID.fromString(KEEP_ALIVE_UUID), BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE );

            gattService.addCharacteristic(primary);
            gattService.addCharacteristic(keepAlive);

            gattServer = ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).openGattServer(context, bluetoothGattServerCallback);
            gattServer.addService(gattService);
        }
    }

    public void startScan() {
        bleScanner.startScan(mConfig.getServiceUUID());
    }

    public void stopScan() {
        bleScanner.stopScan();
    }

    public void advertise()
    {
        bleAdvertiser.startAdvertise(mConfig.getServiceUUID());
    }

    public void stopAdvertise() {
        bleAdvertiser.stopAdvertise();
    }


    public List<Device> getAllDevices() {
        return DBClient.getInstance(context).getAllDevices();
    }

    public void clearAllDevices() {
        DBClient.getInstance(context).clearAllDevices();
    }

    public List<Scan> getAllScans() {
        return DBClient.getInstance(context).getAllScans();
    }

    public List<Scan> getScansByKey(String pubKey) {
        return DBClient.getInstance(context).getScansByKey(pubKey);
    }

    public List<Contact> getAllContacts() { return DBClient.getInstance(context).getAllContacts(); }

    public void wipeDatabase() {
        DBClient.getInstance(context).deleteDatabase();
    }

    public void setEventToJSDispatcher(EventToJSDispatcher eventToJSDispatcher) {
        mEventToJSDispatcher = eventToJSDispatcher;
    }

    public List<Event> getAllAdvertiseData() {
        return DBClient.getInstance(context).getEventsByActionType(Constants.ACTION_ADVERTISE);
    }

    public List<Event> getAllScansData() {
        return DBClient.getInstance(context).getEventsByActionType(Constants.ACTION_SCAN);
    }

    @Override
    public void onEvent(String event, Object data) {
        if (mEventToJSDispatcher != null) {
            mEventToJSDispatcher.onEvent(event, data);
        } else {
            Log.d(TAG, "BLEManager onEvent() | cannot send to JSDispatcher - it's null | event = " + event + ", data = " + data);
            // can be null if application flow started from BroadcastReceiver (boot/restart/upgrade).
            // Service information is still saved to db, but ReactNative UI will not be updated.
        }
    }

    private Device getNewDevice(int rssi, BluetoothDevice device, int tx, String scannedToken, long firstSeenTime, long lastSeenTime) {
        return new Device(firstSeenTime, lastSeenTime, scannedToken, device.getAddress(),
                BLEManager.BLEProtocol.GAP.toString(), rssi, tx);
    }

    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {

                Log.e(TAG, "BluetoothGattServerCallback#onConnectionStateChange: STATE_CONNECTED");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e(TAG, "BluetoothGattServerCallback#onConnectionStateChange: STATE_DISCONNECTED ");
                gattServer.cancelConnection(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicReadRequest: for device " + device.getAddress());

            byte[] fullResponse = new byte[0];
            for (int i = 0; i < filteredContactListToSend.size(); i++) {
                if (i == CONTACTS_FROM_DATABASE_CONSTRAINTS) {
                    break;
                }
                Contact contactToSend = filteredContactListToSend.get(i);
                byte[] currentContact = BytesUtils.byteConcatenation(contactToSend.getEphemeral_id(), String.valueOf(contactToSend.getTimestamp()).getBytes());
                fullResponse = BytesUtils.byteConcatenation(fullResponse, currentContact);
            }
            Log.e(TAG, "BluetoothGattServerCallbackonCharacteristicReadRequest: response string is " + new String(fullResponse));
            boolean isSendResponseSucceded = gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, fullResponse);
            if(isSendResponseSucceded) {
                Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicReadRequest: successfully sent response");
            }
            else {
                Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicReadRequest: failed to send response");
            }

        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, final byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicWriteRequest: value is " + new String(value) + " for device " + device.getAddress());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    Device oldDevice = DBClient.getInstance(context).getDeviceByKey(new String(value)); // get device from database
                    Device newDevice;

                    if (oldDevice != null) {
                        newDevice = getNewDevice(0,device,0,new String(value), oldDevice.getFirstTimestamp(), System.currentTimeMillis());
//                        if (hasUpdateRequirements(oldDevice, result)) {
                        DBClient.getInstance(context).updateDevice(newDevice);
//                        }
                    } else {
                        newDevice = getNewDevice(0,device,0,new String(value), System.currentTimeMillis(), System.currentTimeMillis());
                        DBClient.getInstance(context).addDevice(newDevice);
                    }

                    Scan newScan = new Scan(System.currentTimeMillis(),
                            new String(value),
                            device.getAddress(),
                            BLEManager.BLEProtocol.GAP.toString(),
                            0,
                            0, 0, new float[6],
                            new float[6], SensorUtils.getBatteryPercentage(context));

                    DBClient.getInstance(context).addScan(newScan);

                    int currentTime = (int) (System.currentTimeMillis() / 1000);

                    byte[] rssi = BytesUtils.numToBytes(0, 4);

                    double lat = 0;
                    double lon = 0;
                    try {
                        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        if (locationManager != null) {

                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

                    CryptoManager.getInstance(context).mySelf.storeContact(value, rssi, currentTime, sGeoHash, lat, lon, System.currentTimeMillis());
                    List<Contact> contacts = DBClient.getInstance(context).getAllContactsWithGattServerConnection(CONTACTS_INSERTION_TIME_CONSTRAINTS);
                    Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicWriteRequest: fetched  " + contacts.size() + " from database" );

                    Map<String, Contact> filteredMap = new HashMap<>();
                    for(Contact contact : contacts) {
                        if(!filteredMap.containsKey(new String(contact.getEphemeral_id()))) {
                            filteredMap.put(new String(contact.getEphemeral_id()), contact);
                        }
                    }
                    filteredContactListToSend = new ArrayList<>(filteredMap.values());
                    Log.e(TAG, "BluetoothGattServerCallback#onCharacteristicWriteRequest: after filtration  " + filteredContactListToSend.size());
                }
            });

            if(responseNeeded) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, ("success write").getBytes());
            }
        }
    };
}