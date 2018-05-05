package com.example.android.lab1.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable
{
    private String mIsbn;
    private String mTitle;
    private ArrayList<String> mAuthors;
    private String mPublisher;
    private Integer mPublishYear;
    private int mCondition;
    private Address mAddress;
    private String mUid; // user id
    private String mWebThumbnail;
    private List<String> mUserBookPhotosWebStoragePath;

    public Book(){}

    public String getWebThumbnail() {
        return mWebThumbnail;
    }

    public void setWebThumbnail(String thumbnail) {
        mWebThumbnail = thumbnail;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getIsbn() {
        return mIsbn;
    }

    public void setIsbn(String mIsbn) {
        this.mIsbn = mIsbn;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getPublisher() {
        return mPublisher;
    }

    public void setPublisher(String publisher) {
        mPublisher = publisher;
    }

    public Integer getPublishYear() {
        return mPublishYear;
    }

    public void setPublishYear(Integer publishYear) {
        mPublishYear = publishYear;
    }

    public int getCondition() {
        return mCondition;
    }

    public void setConditions(int condition) {
        mCondition = condition;
    }

    public Address getAddress() {
        return mAddress;
    }

    public void setAddress(Address address) {
        mAddress = address;
    }


    public List<String> getUserBookPhotosWebStoragePath() {
        return mUserBookPhotosWebStoragePath;
    }

    public void setUserBookPhotosWebStoragePath(ArrayList<String> mUserBookPhotosWebStoragePath) {
        this.mUserBookPhotosWebStoragePath = mUserBookPhotosWebStoragePath;
    }

    public void addAuthor(String author) {
        if (mAuthors == null)
            mAuthors = new ArrayList<>();
        mAuthors.add(author);
    }

    public ArrayList<String> getAuthors() {
        return mAuthors;
    }
}
