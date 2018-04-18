package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Book
{
    private String mIsbn;
    private String mTitle;
    private String mAuthor;
    private String mPublisher;
    private int    mPublishYear;
    private Condition mConditions;
    private String mAddress;
    private String mUid;
    private String mThumbnail;
    private List<String> mUserPhotosPath;

    public Book(){}

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public List<String> getUserPhotosPath() {
        return mUserPhotosPath;
    }

    public String getIsbn() {
        return mIsbn;
    }

    public void setIsbn(String mIsbn) {
        mIsbn = mIsbn;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getPublisher() {
        return mPublisher;
    }

    public void setPublisher(String publisher) {
        mPublisher = publisher;
    }

    public int getPublishYear() {
        return mPublishYear;
    }

    public void setPublishYear(int publishYear) {
        mPublishYear = publishYear;
    }

    public Condition getConditions() {
        return mConditions;
    }

    public void setConditions(Condition conditions) {
        mConditions = conditions;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

}
