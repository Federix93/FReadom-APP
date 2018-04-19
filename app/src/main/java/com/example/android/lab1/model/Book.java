package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class Book
{
    private String mIsbn;
    private String mTitle;
    private String mAuthor;
    private String mPublisher;
    private int    mPublishYear;
    private String mCondition;
    private String mAddress;
    private String mUid;
    private String mThumbnail;
    private List<String> bookImagesUrls;

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

    public String getCondition() {
        return mCondition;
    }

    public void setConditions(String condition) {
        mCondition = condition;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public List<String> getBookImagesUrls() {
        return bookImagesUrls;
    }

    public void setBookImagesUrls(ArrayList<String> bookImagesUrls) {
        this.bookImagesUrls = bookImagesUrls;
    }
}
