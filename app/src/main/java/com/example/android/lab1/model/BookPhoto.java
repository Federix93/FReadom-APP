package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BookPhoto implements Parcelable {

    private String mUrl;
    private String mTitle;

    public BookPhoto(String url, String title) {
        mUrl = url;
        mTitle = title;
    }

    protected BookPhoto(Parcel in) {
        mUrl = in.readString();
        mTitle = in.readString();
    }

    public static final Creator<BookPhoto> CREATOR = new Creator<BookPhoto>() {
        @Override
        public BookPhoto createFromParcel(Parcel in) {
            return new BookPhoto(in);
        }

        @Override
        public BookPhoto[] newArray(int size) {
            return new BookPhoto[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public static  BookPhoto[] getSpacePhotos() {

        return new BookPhoto[]{
                new BookPhoto("http://i.imgur.com/qpr5LR2.jpg", "Earth"),
                new BookPhoto("http://i.imgur.com/pSHXfu5.jpg", "Astronaut"),
                new BookPhoto("http://i.imgur.com/3wQcZeY.jpg", "Satellite"),
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mUrl);
        parcel.writeString(mTitle);
    }
}
