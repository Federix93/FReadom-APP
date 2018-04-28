package com.example.android.lab1.model;

/**
 * Created by Federico on 20/03/2018.
 */

public class User {

    private static User instance = null;

    private String mUsername;
    private String mEmail;
    private String mPhone;
    private String mAddress;
    private String mImage;
    private String mShortBio;
    private Float mRating;

    private User(){}

    public static User getInstance(){
        if(instance == null)
            instance = new User();
        return instance;
    }

    public String getShortBio(){ return mShortBio; }

    public void setShortBio(String pShortBio){ mShortBio = pShortBio; }

    public String getImage() {
        return mImage;
    }

    public void setImage(String pImage) {
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

    public Float getRating ()
    {
        return mRating;
    }

    public void setRating(Float rating)
    {
        mRating = rating;
    }

    public static class Utils{
        public static final String USERNAME_KEY = "username";
        public static final String EMAIL_KEY = "email";
        public static final String PICTURE_KEY = "image";
        public static final String POSITION_KEY = "address";
        public static final String PHONE_KEY = "phone";
        public static final String SHORTBIO_KEY= "shortBio";
    }
}
