package com.wix.specialble.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wix.specialble.kays.PublicKey;

import java.util.List;

@Dao
public interface PublicKeyDao {

    @Query("SELECT * FROM publickey WHERE id = :index")
    PublicKey getPKByIndex(int index);

    @Insert
    void insertAll(List<PublicKey> pks);

    @Update
    void update(PublicKey publicKey);

    @Insert
    void insert(PublicKey publicKey);

    @Delete
    void delete(PublicKey publicKey);

    @Query("DELETE FROM publicKey")
    public void clearAll();
}
