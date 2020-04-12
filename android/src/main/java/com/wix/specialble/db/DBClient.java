package com.wix.specialble.db;

import android.content.Context;

import androidx.room.Room;

import com.wix.specialble.bt.Device;
import com.wix.specialble.kays.PublicKey;

import java.util.List;


public class DBClient {

    private Context context;
    private static DBClient sDBClientInstance;
    private SpecialBLEDatabase bleDevicesDB;
    private PublicKeysDatabase publicKeysDB;
    private static final String TAG = "DBClient";


    private DBClient(Context context) {
        this.context = context;
        bleDevicesDB = Room.databaseBuilder(context, SpecialBLEDatabase.class, "BLEDevices").build();
        publicKeysDB = Room.databaseBuilder(context, PublicKeysDatabase.class, "PublicKeys").build();
    }

    public static DBClient getInstance(Context context) {
        if (sDBClientInstance == null) {
            sDBClientInstance = new DBClient(context);
        }
        return sDBClientInstance;
    }



    public SpecialBLEDatabase getSpecialBLEDB() {
        return bleDevicesDB;
    }

    public Device getDeviceByKey(String pk) {
        return bleDevicesDB.deviceDao().getDeviceByKey(pk);
    }

    public void updateDevice(Device device){
        bleDevicesDB.deviceDao().update(device);
    }

    public void addDevice(Device newDevice){
        bleDevicesDB.deviceDao().insert(newDevice);
    }

    public List<Device> getAllDevices() {
       return bleDevicesDB.deviceDao().getAllBLEDevices();
    }

    public void clearAllDevices() {
        bleDevicesDB.deviceDao().clearAll();
    }

    public void insertAllKeys(List<PublicKey> pkList) {
        publicKeysDB.publicKeyDao().insertAll(pkList);
    }

}
