package com.example.android.lab1.model.chatmodels;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String mPhotoURL;
    private String mUsername;
    private static Map<String, String> mMapUserIDChatID = new HashMap<>();;

    public User(){}

    public User(String username, String photoURL){
        mUsername = username;
        mPhotoURL = photoURL;

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

    public void setMapUserIDChatID(String memberID, String chatID){
        mMapUserIDChatID.put(memberID, chatID);
    }

    public String getChatID(String memberID){
        return mMapUserIDChatID.get(memberID);
    }
}
