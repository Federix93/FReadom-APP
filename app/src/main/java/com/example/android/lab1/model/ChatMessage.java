package com.example.android.lab1.model;

import android.net.Uri;

public class ChatMessage {

    private String mUsername;
    private String mUid;
    private String mTextMessage;
    private String mPhotoURL;

    public ChatMessage(){}

    public ChatMessage(String pUsername, String pTextMessage, String pPhotoURL){
        mUsername = pUsername;
        mTextMessage = pTextMessage;
        mPhotoURL = pPhotoURL;
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

    public String getUid() {
        return mUid;
    }

    public void setUid(String pUid) {
        this.mUid = pUid;
    }

    public String getTextMessage() {
        return mTextMessage;
    }

    public void setTextMessage(String pTextMessage) {
        this.mTextMessage = pTextMessage;
    }
}
