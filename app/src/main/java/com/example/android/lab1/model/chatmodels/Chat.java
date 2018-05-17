package com.example.android.lab1.model.chatmodels;

import java.util.Date;

public class Chat {

    private String mLastMessage;
    private long mTimestamp;
    private String mBookID;
    private int mCounter;

    public Chat(){}

    public Chat(String bookID, String lastMessage, long timestamp){
        mBookID = bookID;
        mLastMessage = lastMessage;
        mTimestamp = timestamp;
    }

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

    public void setCounter(int counter){
        mCounter = counter;
    }

    public int getCounter() { return mCounter; }

}
