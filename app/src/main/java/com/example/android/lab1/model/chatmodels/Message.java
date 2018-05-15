package com.example.android.lab1.model.chatmodels;

import android.net.Uri;

import java.util.Date;

public class Message {

    private String mTextMessage;
    private String mPhotoURL;
    private String mUsername;
    private String mSenderId;
    private long mTimestamp;
    private String mPhotoProfileURL;

    public Message(){}

    public Message(String pUsername, String senderId, String pTextMessage, String pPhotoProfileURL, long pTimestamp, String pPhotoURL){
        mUsername = pUsername;
        mSenderId = senderId;
        mTextMessage = pTextMessage;
        mTimestamp = pTimestamp;
        mPhotoProfileURL = pPhotoProfileURL;
        mPhotoURL = pPhotoURL;
    }

    public String getPhotoProfileURL() {
        return mPhotoProfileURL;
    }

    public void setPhotoProfileURL(String pPhotoProfileURL) {
        this.mPhotoProfileURL = pPhotoProfileURL;
    }

    public String getPhotoURL() {
        return mPhotoURL;
    }

    public void setPhotoURL(String photoURL) {
        mPhotoURL = photoURL;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String pUsername) {
        this.mUsername = pUsername;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long pTimestamp) { this.mTimestamp = pTimestamp; }

    public String getTextMessage() {
        return mTextMessage;
    }

    public void setTextMessage(String pTextMessage) {
        this.mTextMessage = pTextMessage;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderId(String pSenderId) {
        this.mSenderId = pSenderId;
    }


}
