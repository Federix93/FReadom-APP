package com.example.android.lab1;

import android.net.Uri;

public class UserManager {

    private static User instance = new User();

    private UserManager(){}

    public static User getUser(){
        return instance;
    }

    protected static class User {

        private String mUsername;
        private String mEmail;
        private String mPhone;
        private String mAddress;
        private Uri mImage;

        private User(){}

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

}
