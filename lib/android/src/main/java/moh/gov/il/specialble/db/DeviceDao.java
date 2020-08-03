package moh.gov.il.specialble.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import moh.gov.il.specialble.bt.Device;

import java.util.List;

@Dao
public interface DeviceDao {

    @Query("SELECT * FROM device")
    List<Device> getAllBLEDevices();

    @Query("SELECT * FROM device WHERE publicKey IN (:publicKey)")
    List<Device> getDeviceByKeys (String[] publicKey);

    @Query("SELECT * FROM device WHERE publicKey = :publicKey")
    Device getDeviceByKey (String publicKey);

    @Insert
    void insertAll(Device... devices);

    @Update
    void update(Device device);

    @Insert
    void insert(Device device);

    @Delete
    void delete(Device device);

    @Query("DELETE FROM device")
    public void clearAll();
}
