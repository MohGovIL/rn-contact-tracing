package moh.gov.il.specialble.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import moh.gov.il.specialble.kays.PublicKey;

@Database(entities = {PublicKey.class}, version = 1)
public abstract class PublicKeysDatabase extends RoomDatabase {
    public abstract PublicKeyDao publicKeyDao();
}
