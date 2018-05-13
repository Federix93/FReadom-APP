package com.example.android.lab1.model.chatmodels;

import java.util.Date;

public class Chat {

    private String mLastMessage;
    private long mTimestamp;
    private String mBookID;

    public String getBookID() {
        return mBookID;
    }

    public void setBookID(String pBookID) {
        this.mBookID = pBookID;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.mLastMessage = lastMessage;
    }


}
