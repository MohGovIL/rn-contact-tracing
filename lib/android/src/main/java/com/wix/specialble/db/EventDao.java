package com.wix.specialble.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wix.specialble.bt.Event;

import java.util.List;

@Dao
public interface EventDao {

    @Query("SELECT * FROM events")
    List<Event> getAllEvents();

    @Query("SELECT * FROM events WHERE action_type = :actionType ORDER BY device_name,timestamp desc")
    List<Event> getEventsByActionType(String actionType);

    @Insert
    void insertAll(Event... events);

    @Update
    void update(Event event);

    @Insert
    void insert(Event event);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM events")
    public void clearAll();
}
