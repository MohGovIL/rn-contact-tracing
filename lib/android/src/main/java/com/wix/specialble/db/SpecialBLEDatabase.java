package com.wix.specialble.db;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.wix.crypto.Contact;
import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Event;
import com.wix.specialble.bt.Scan;

@Database(entities = {Device.class, Scan.class, Contact.class, Event.class}, version = 5, exportSchema = true)
public abstract class SpecialBLEDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
    public abstract ScanDao scanDao();
    public abstract ContactDao contactDao();
    public abstract EventDao eventDao();
}

