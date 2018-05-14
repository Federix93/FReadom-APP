package com.example.android.lab1.model.chatmodels;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String mPhotoURL;
    private String mUsername;
    private Map<String, String> mMapUserIDChatID;

    public User(){}

    public User(String username, String photoURL, Map<String, String> map){
        mUsername = username;
        mPhotoURL = photoURL;
        mMapUserIDChatID = map;
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

    public Map<String, String> getMapUserIDChatID() { return mMapUserIDChatID; }

    public String getChatID(String memberID){
        return mMapUserIDChatID.get(memberID);
    }

}
