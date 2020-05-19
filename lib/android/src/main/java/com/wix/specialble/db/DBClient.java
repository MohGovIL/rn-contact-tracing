package com.wix.specialble.db;

import android.content.Context;

import androidx.room.Room;

import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Scan;
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

    public void insertAllKeys(List<PublicKey> pkList) {
        publicKeysDB.publicKeyDao().insertAll(pkList);
    }

    /***********
     * Devices *
     ***********/
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

    /***********
     *  Scans  *
     ***********/
    public List<Scan> getScansByKey(String pk) {
        return bleDevicesDB.scanDao().getScansByKey(pk);
    }

    public void updateScan(Scan scan){
        bleDevicesDB.scanDao().update(scan);
    }

    public void addScan(Scan newScan){
        bleDevicesDB.scanDao().insert(newScan);
    }

    public List<Scan> getAllScans() {
       return bleDevicesDB.scanDao().getAllBLEScans();
    }

    public void clearAllScans() {
        bleDevicesDB.scanDao().clearAll();
    }

    /***********
     *  Events  *
     ***********/

    public List<Event> getAllEvents() { return  bleDevicesDB.eventDao().getAllEvents(); }

    public List<Event> getEventsByActionType(String actionType) { return  bleDevicesDB.eventDao().getEventsByActionType(actionType); }

    public void insertAll(Event... events) { bleDevicesDB.eventDao().insertAll(events); }

    public void insert(Event event) { bleDevicesDB.eventDao().insert(event); }

    public void update(Event event) { bleDevicesDB.eventDao().update(event); }

    public void delete(Event event) { bleDevicesDB.eventDao().delete(event); }

    public void clearAllEvents() { bleDevicesDB.eventDao().clearAll(); }
    
}
