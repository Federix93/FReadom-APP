package com.example.android.lab1.model.chatmodels;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String mPhotoURL;
    private String mUsername;
    private String mUserToken;

    public User(){}

    public User(String username, String photoURL, String token){
        mUsername = username;
        mPhotoURL = photoURL;
        mUserToken = token;
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

    public String getUserToken() {
        return mUserToken;
    }

    public void setUserToken(String userToken) {
        this.mUserToken = userToken;
    }


}
