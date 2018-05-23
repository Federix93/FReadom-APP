package com.example.android.lab1.model.chatmodels;

public class Chat {

    private String mLastMessage;
    private long mTimestamp;
    private String mBookID;
    private int mCounter;
    private String mSenderUID;
    private String mReceiverUID;

    public Chat(){}

    public Chat(String bookID, String receiverUID, String lastMessage, long timestamp){
        mBookID = bookID;
        mLastMessage = lastMessage;
        mTimestamp = timestamp;
        mReceiverUID = receiverUID;
    }

    public String getSenderUID() {
        return mSenderUID;
    }

    public void setSenderUID(String lastMessageUserID) {
        this.mSenderUID = lastMessageUserID;
    }

    public String getReceiverUID() {
        return mReceiverUID;
    }

    public void setReceiverUID(String receiverUID) {
        this.mReceiverUID = receiverUID;
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
