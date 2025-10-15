package com.example.mailit.beans;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mailit.utils.DateConverter;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

@Entity
public class Email implements Serializable {

    @PrimaryKey
    @NonNull
    @SerializedName("objectId")
    private String id;
    private String sender;
    private String receiver;
    private String subject;
    private String content;
    @TypeConverters(DateConverter.class)
    private Date date;
    @ColumnInfo(name = "user_id")
    private String userId;
    private String type;
    @ColumnInfo(name = "user_profile")
    private String userProfile;
    @ColumnInfo(name = "read_status")
    private boolean isRead;
    @Ignore
    private String keyWord;

    public Email(@NonNull String id, String sender, String receiver, String subject, String content, Date date, String userId, String type, String userProfile) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.date = date;
        this.userId = userId;
        this.userProfile = userProfile;
        this.type = type;
    }

    @Ignore
    public Email(String receiver, String subject, String content, String userId, String type) {
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.userId = userId;
        this.type = type;
        isRead = false;
    }

    @Ignore
    public Email(@NonNull String id, String receiver, String subject, String content, String userId, String type) {
        this.id = id;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.userId = userId;
        this.type = type;
        isRead = false;
    }

    @Ignore
    public Email(String info) {
        this.userId = info;
        this.receiver = info;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }
}
