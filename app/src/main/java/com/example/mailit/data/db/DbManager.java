package com.example.mailit.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mailit.beans.Email;
import com.example.mailit.beans.User;
import com.example.mailit.data.db.dao.EmailDao;
import com.example.mailit.data.db.dao.UserDao;

@Database(entities = {Email.class, User.class}, version = 1)
public abstract class DbManager extends RoomDatabase {

    public abstract EmailDao emailDao();

    public abstract UserDao userDao();

    private static DbManager instance = null;

    public static DbManager getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, DbManager.class, "mailit-db").build();
        }
        return instance;
    }
}
