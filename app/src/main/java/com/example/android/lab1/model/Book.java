package com.example.android.lab1.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Book implements Serializable
{
    private String mIsbn;
    private String mTitle;
    private String mAuthors;
    private String mPublisher;
    private Integer mPublishYear;
    private int mCondition;
    private String mAddress;
    private Double lon;
    private Double lat;
    private String mUid; // user id
    private String mWebThumbnail;
    private List<String> mBookPhotosPaths;
    private Date mLoanStart;
    private Date mLoanEnd;
    private int genre;

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

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public List<String> getUserBookPhotosStoragePath() {
        return mBookPhotosPaths;
    }

    public void setUserBookPhotosStoragePath(ArrayList<String> mUserBookPhotosWebStoragePath) {
        this.mBookPhotosPaths = mUserBookPhotosWebStoragePath;
    }

    public void addAuthor(String author) {
        if (mAuthors == null)
            mAuthors = author;
        else
            mAuthors += "," + author;
    }

    public String getAuthors() {
        return mAuthors;
    }

    public Date getLoanStart() {
        return mLoanStart;
    }

    public void setLoanStart(Calendar loanStart) {
        this.mLoanStart = loanStart != null ? loanStart.getTime() : null;
    }

    public Date getLoanEnd() {
        return mLoanEnd;
    }

    public void setLoanEnd(Calendar loanEnd) {
        this.mLoanEnd = loanEnd != null ? loanEnd.getTime() : null;
    }

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }
}
