package com.example.android.lab1.model.chatmodels;

public class User {

    private String mPhotoURL;
    private String mUsername;

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


}
