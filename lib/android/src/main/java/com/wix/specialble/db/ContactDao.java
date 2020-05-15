package com.wix.specialble.db;

import android.database.Cursor;

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

    @Query("SELECT * FROM Contacts order by id asc")
    Cursor getCursorAll();



    @Query("DELETE FROM Contacts where timestamp < :history")
    public void deleteContactHistory(int history);

}
