package com.wix.specialble.bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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
import android.util.Log;

import androidx.core.app.ActivityCompat;

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
import java.util.UUID;

public class BLEScannerManager {

    private static final String SCANNING_STATUS = "scanningStatus";
    private static final String FOUND_DEVICE = "foundDevice";
    private static final String FOUND_SCAN = "foundScan";
    private RotationVectorManager mRotationVectorManager;
    private AccelerometerManager mAccelerometerManager;
    private ProximityManager mProximityManager;

    Context mContext;
    BluetoothAdapter bluetoothAdapter;
    SpecialBLEScanCallback bleScanCallback;
    DBClient dbClient;

    private String TAG = "BLEScannerManager";
    private IEventListener mEventListenerCallback;


    // this is a place holder for Geo-Hash Data
    // it should be injected from the application container level on every change from LocationManager
    // TODO: provide external API to the react native level
    ///////////////////////////////////////////////////////////////
    public static byte[] sGeoHash = new byte[]{0, 0, 0, 0, 0};

    BLEScannerManager(Context context, IEventListener eventListenerCallback) {
        mContext = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleScanCallback = new SpecialBLEScanCallback();
        dbClient = DBClient.getInstance(context);
        mEventListenerCallback = eventListenerCallback;

        // declare sensors
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mProximityManager = new ProximityManager(sensorManager);
        mAccelerometerManager = new AccelerometerManager(sensorManager);
        mRotationVectorManager = new RotationVectorManager(sensorManager);

    }

    public void startScan(String serviceUUID) {
        bluetoothAdapter.enable();
        if (bluetoothAdapter.isEnabled()) {
            registerSensors();

            Config config = Config.getInstance(mContext);
            int scanMatchMode = config.getScanMatchMode();

            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID))).build();
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);

            ScanSettings settings = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                settings = new ScanSettings.Builder().setMatchMode(scanMatchMode).build();
            } else {
                settings = new ScanSettings.Builder().build();
            }

            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, bleScanCallback);
            mEventListenerCallback.onEvent(SCANNING_STATUS, true);
        }
    }

    public void stopScan() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(bleScanCallback);
        mEventListenerCallback.onEvent(SCANNING_STATUS, false);
        unregisterSensors();

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
                unregisterSensors();
            }
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DBClient.getInstance(mContext).insert(new Event(System.currentTimeMillis(), "none", Constants.ACTION_SCAN, "failure", String.valueOf(errorCode)));
                }
            });
        }
    }

    private void handleScanResults(final ScanResult result, final String scannedToken, final int tx, final byte[] byteScannedToken) {
        // handle devices
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DBClient.getInstance(mContext).insert(new Event(System.currentTimeMillis(), scannedToken, Constants.ACTION_SCAN, "success", ""));

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
                        tx, mProximityManager.getEvents()[0], mAccelerometerManager.getEvents(), mRotationVectorManager.getEvents(), SensorUtils.getBatteryPercentage(mContext));

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
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
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

//                if(byteScannedToken.length == Constants.KEY_LEN) {
                CryptoManager.getInstance(mContext).mySelf.storeContact(byteScannedToken, rssi, currentTime, sGeoHash, lat, lon);
//                }

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
}