package com.example.mailit.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mailit.beans.Email;

import java.util.List;

@Dao
public interface EmailDao {

    @Query("SELECT * FROM Email WHERE receiver = :email AND type = :type ORDER BY date DESC")
    List<Email> getAllInbox(String email, String type);

    @Query("SELECT * FROM Email WHERE user_id = :userId AND type = :type ORDER BY date DESC")
    List<Email> getAllSent(String userId, String type);

    @Query("SELECT * FROM Email WHERE user_id = :userId AND type = :type ORDER BY date DESC")
    List<Email> getAllDraft(String userId, String type);

    @Query("SELECT * FROM EMAIL WHERE (receiver LIKE '%' || :keyWord || '%' OR subject LIKE '%' || :keyWord || '%' OR content LIKE '%' || :keyWord || '%') AND receiver = :email AND type = :type ORDER BY date DESC")
    List<Email> getAllInboxSearch(String email, String type, String keyWord);

    @Query("SELECT * FROM EMAIL WHERE (receiver LIKE '%' || :keyWord || '%' OR subject LIKE '%' || :keyWord || '%' OR content LIKE '%' || :keyWord || '%') AND user_id = :userId AND type = :type ORDER BY date DESC")
    List<Email> getAllSentSearch(String userId, String type, String keyWord);

    @Query("SELECT * FROM EMAIL WHERE (receiver LIKE '%' || :keyWord || '%' OR subject LIKE '%' || :keyWord || '%' OR content LIKE '%' || :keyWord || '%') AND user_id = :userId AND type = :type ORDER BY date DESC")
    List<Email> getAllDraftSearch(String userId, String type, String keyWord);

    @Insert
    long insert(Email email);

    @Delete
    int delete(Email email);

    @Update
    int update(Email email);
}
