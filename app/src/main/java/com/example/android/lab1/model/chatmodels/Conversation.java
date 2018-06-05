package com.example.android.lab1.model.chatmodels;

public class Conversation {
    User mUser;
    String mChatId;
    Long mTimestamp;
    Chat mChat;
    String mUserId;

    public Conversation(User mUser, String mChatId) {
        this.mUser = mUser;
        this.mChatId = mChatId;
        this.mTimestamp = Long.valueOf("0");
    }

    public Conversation(User mUser, String mChatId, Long mTimestamp) {
        this.mUser = mUser;
        this.mChatId = mChatId;
        this.mTimestamp = mTimestamp;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public Chat getChat() {
        return mChat;
    }

    public void setChat(Chat chat) {
        this.mChat = chat;
        this.mTimestamp = chat.getTimestamp();
    }

    @Override
    public boolean equals(Object obj) {
        Conversation other = (Conversation) obj;
        return mChatId.equals(other.getChatId()) &&
                mTimestamp.equals(other.getTimestamp());
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    public String getChatId() {
        return mChatId;
    }

    public void setChatId(String mChatId) {
        this.mChatId = mChatId;
    }

    public Long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.mTimestamp = mTimestamp;
    }
}