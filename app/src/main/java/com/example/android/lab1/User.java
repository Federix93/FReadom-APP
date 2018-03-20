package com.example.android.lab1;

import android.net.Uri;

/**
 * Created by Federico on 20/03/2018.
 */

public class User {

    private static User instance = null;

    private String mUsername;
    private String mEmail;
    private String mPhone;
    private String mAddress;
    private Uri mImage;


    private User(){}

    public static User getInstance(){
        if(instance == null)
            instance = new User();
        return instance;
    }

    public Uri getImage() {
        return mImage;
    }

    public void setImage(Uri pImage) {
        mImage = pImage;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String pAddress) {
        mAddress = pAddress;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String pUsername) {
        mUsername = pUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String pEmail) {
        mEmail = pEmail;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String pPhone) {
        mPhone = pPhone;
    }
}
