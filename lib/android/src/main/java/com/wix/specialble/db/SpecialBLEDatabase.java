package com.wix.specialble.db;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.wix.specialble.bt.Device;
import com.wix.specialble.kays.PublicKey;

@Database(entities = {Device.class}, version = 1)
public abstract class SpecialBLEDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
}

