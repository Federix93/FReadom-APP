package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Federico on 20/03/2018.
 */

public class User implements Parcelable {

    private static User instance = null;

    private String mUsername;
    private String mEmail;
    private String mPhone;
    private String mAddress;
    private String mImage;
    private String mShortBio;
    private Float mRating;
    private int mNumRatings;

    public User(){
        setRating(0f);
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

    @Override
    public int describeContents() {
        return 0;
    }

    private User(Parcel in) {
        mUsername = in.readString();
        mEmail = in.readString();
        mPhone = in.readString();
        mAddress = in.readString();
        mImage = in.readString();
        mShortBio = in.readString();
        mRating = in.readFloat();
        mNumRatings = in.readInt();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mEmail);
        dest.writeString(mPhone);
        dest.writeString(mAddress);
        dest.writeString(mImage);
        dest.writeString(mShortBio);
        dest.writeFloat(mRating);
        dest.writeInt(mNumRatings);
    }

    public int getNumRatings() {
        return mNumRatings;
    }

    public void setNumRatings(int mNumRatings) {
        this.mNumRatings = mNumRatings;
    }
}
