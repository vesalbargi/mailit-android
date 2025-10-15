package com.example.mailit.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mailit.beans.User;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User WHERE id = :id")
    User getOne(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);
}
