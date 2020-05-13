package com.wix.specialble.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.wix.crypto.Contact;

import java.util.List;

/**
 * Created by hagai on 11/05/2020.
 */

@Dao
public interface ContactDao {

    @Query("SELECT * FROM Contacts")
    List<Contact> getAllContacts();

    @Insert
    void insert(Contact contact);

    @Insert
    void insertAll(Contact... contacts);

    @Delete
    void delete(Contact contact);

    @Delete
    void delete(Contact... contacts);


    @Query("DELETE FROM Contacts")
    public void clearAll();
}
