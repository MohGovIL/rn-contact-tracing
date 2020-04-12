package com.wix.specialble.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.wix.specialble.kays.PublicKey;

@Database(entities = {PublicKey.class}, version = 1)
public abstract class PublicKeysDatabase extends RoomDatabase {
    public abstract PublicKeyDao publicKeyDao();
}
