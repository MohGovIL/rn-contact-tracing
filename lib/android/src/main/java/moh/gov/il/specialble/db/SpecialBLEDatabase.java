package moh.gov.il.specialble.db;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import moh.gov.il.crypto.Contact;
import moh.gov.il.specialble.bt.Device;
import moh.gov.il.specialble.bt.Event;
import moh.gov.il.specialble.bt.Scan;

@Database(entities = {Device.class, Scan.class, Contact.class, Event.class}, version = 6, exportSchema = true)
public abstract class SpecialBLEDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
    public abstract ScanDao scanDao();
    public abstract ContactDao contactDao();
    public abstract EventDao eventDao();
}

