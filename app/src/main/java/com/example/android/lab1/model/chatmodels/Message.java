package com.example.android.lab1.model.chatmodels;

import com.google.firebase.firestore.ServerTimestamp;

public class Message {

    private String mTextMessage;
    private String mPhotoURL;
    private String mSenderId;
    @ServerTimestamp
    private Long mTimestamp;

    public Message(){}

    public Message(String senderId, String pTextMessage, Long pTimestamp, String pPhotoURL) {
        mSenderId = senderId;
        mTextMessage = pTextMessage;
        mTimestamp = pTimestamp;
        mPhotoURL = pPhotoURL;
    }

    public String getPhotoURL() {
        return mPhotoURL;
    }

    public void setPhotoURL(String photoURL) {
        mPhotoURL = photoURL;
    }

    @ServerTimestamp
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
