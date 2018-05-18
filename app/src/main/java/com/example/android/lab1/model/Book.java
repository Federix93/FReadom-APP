package com.example.android.lab1.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;


public class Book implements Parcelable, Comparable<Book> {

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    private static final String ISBN = "ISBN";
    private static final String TITLE = "TITLE";
    private static final String PHOTOS_PATH = "PHOTOS_PATH";
    private static final String WEB_THUMBNAIL = "WEB_THUMBNAIL";
    private static final String UUID = "UUID";
    private static final String ADDRESS = "ADDRESS";
    private static final String PUBLISHER = "PUBLISHER";
    private static final String AUTHORS = "AUTHORS";
    private static final String LAT = "LAT";
    private static final String LON = "LON";
    private static final String PUBLISH_YEAR = "PY";
    private static final String LOAN_START = "LS";
    private static final String LOAN_END = "LE";
    private static final String INFO_LINK = "IL";
    private static final String CONDITIONS = "CONDITIONS";
    private static final String GENRE = "GENRE";
    private static final String TIMESTAMP = "TIMESTAMP";
    private static final String TAGS = "TAGS";

    private String mIsbn;
    private String mTitle;
    private String mAuthors;
    private String mPublisher;
    private Integer mPublishYear;
    private int mCondition;
    private String mAddress;
    private GeoPoint mGeoPoint;
    private String mUid; // user id
    private String mWebThumbnail;
    private ArrayList<String> mBookPhotosPaths;
    private String mSearchTags;
    private Long mLoanStart;
    private Long mLoanEnd;
    private int mGenre;
    private String mInfoLink;
    private Long mTimeInserted;

    public Book() {
    }

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

    public void setCondition(int condition) {
        mCondition = condition;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
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

    public void setAuthors(String authors)
    {
        this.mAuthors = authors;
    }

    public Long getLoanStart() {
        return mLoanStart;
    }

    public void setLoanStart(Long loanStart) {
        this.mLoanStart = loanStart;
    }

    public Long getLoanEnd() {
        return mLoanEnd;
    }

    public void setLoanEnd(Long loanEnd) {
        this.mLoanEnd = loanEnd;
    }

    protected Book(Parcel in) {
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        mIsbn = bundle.containsKey(ISBN) ? bundle.getString(ISBN) : null;
        mTitle = bundle.containsKey(TITLE) ? bundle.getString(TITLE) : null;
        mBookPhotosPaths = bundle.containsKey(PHOTOS_PATH) ? bundle.getStringArrayList(PHOTOS_PATH) : null;
        mWebThumbnail = bundle.containsKey(WEB_THUMBNAIL) ? bundle.getString(WEB_THUMBNAIL) : null;
        mUid = bundle.containsKey(UUID) ? bundle.getString(UUID) : null;
        mAddress = bundle.containsKey(ADDRESS) ? bundle.getString(ADDRESS) : null;
        mPublisher = bundle.containsKey(PUBLISHER) ? bundle.getString(PUBLISHER) : null;
        mAuthors = bundle.containsKey(AUTHORS) ? bundle.getString(AUTHORS) : null;
        mPublishYear = bundle.containsKey(PUBLISH_YEAR) ? bundle.getInt(PUBLISH_YEAR) : null;
        mLoanStart = bundle.containsKey(LOAN_START) ? bundle.getLong(LOAN_START) : null;
        mLoanEnd = bundle.containsKey(LOAN_END) ? bundle.getLong(LOAN_END) : null;
        mInfoLink = bundle.containsKey(INFO_LINK) ? bundle.getString(INFO_LINK) : null;
        mTimeInserted = bundle.containsKey(TIMESTAMP) ? bundle.getLong(TIMESTAMP) : null;
        mSearchTags = bundle.containsKey(TAGS) ? bundle.getString(TAGS) : null;
        if (bundle.containsKey(CONDITIONS))
            mCondition = bundle.getInt(CONDITIONS);
        if (bundle.containsKey(GENRE))
            mGenre = bundle.getInt(GENRE);
        if (bundle.containsKey(LAT) && bundle.containsKey(LON))
            mGeoPoint = new GeoPoint(bundle.getDouble(LAT),
                    bundle.getDouble(LON));

    }

    public int getGenre() {
        return mGenre;
    }

    public void setGenre(int genre) {
        this.mGenre = genre;
    }

    public String getInfoLink() {
        return mInfoLink;
    }

    public void setInfoLink(String infoLink) {
        this.mInfoLink = infoLink;
    }

    public GeoPoint getGeoPoint() {
        return mGeoPoint;
    }

    public void setGeoPoint(GeoPoint mGeoPoint) {
        this.mGeoPoint = mGeoPoint;
    }

    public void setGeoPoint(Double lat, Double lon) {
        setGeoPoint(new GeoPoint(lat, lon));
    }

    public String getAuthor(int i) {
        if (getAuthors() != null) {
            String[] splitted = getAuthors().split(",");
            return i >= 0 && i < splitted.length ? splitted[i] : null;

        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        if (mIsbn != null)
            bundle.putString(ISBN, mIsbn);
        if (mTitle != null)
            bundle.putString(TITLE, mTitle);
        if (mBookPhotosPaths != null && !mBookPhotosPaths.isEmpty())
            bundle.putStringArrayList(PHOTOS_PATH, mBookPhotosPaths);
        if (mWebThumbnail != null)
            bundle.putString(WEB_THUMBNAIL, mWebThumbnail);
        if (mUid != null)
            bundle.putString(UUID, mUid);
        if (mAddress != null)
            bundle.putString(ADDRESS, mAddress);
        if (mPublisher != null)
            bundle.putString(PUBLISHER, mPublisher);
        if (mAuthors != null)
            bundle.putString(AUTHORS, mAuthors);
        if (mGeoPoint != null) {
            bundle.putDouble(LAT, mGeoPoint.getLatitude());
            bundle.putDouble(LON, mGeoPoint.getLongitude());
        }
        if (mPublishYear != null)
            bundle.putInt(PUBLISH_YEAR, mPublishYear);
        if (mLoanStart != null)
            bundle.putLong(LOAN_START, mLoanStart);
        if (mLoanEnd != null)
            bundle.putLong(LOAN_END, mLoanEnd);
        if (mInfoLink != null)
            bundle.putString(INFO_LINK, mInfoLink);
        if (mTimeInserted != null)
            bundle.putLong(TIMESTAMP, mTimeInserted);
        if (mSearchTags != null)
            bundle.putString(TAGS, mSearchTags);
        bundle.putInt(CONDITIONS, mCondition);
        bundle.putInt(GENRE, mGenre);
    }


    public Long getTimeInserted() {
        return mTimeInserted;
    }

    public void setTimeInserted(Long mTimeInserted) {
        this.mTimeInserted = mTimeInserted;
    }

    @Override
    public int compareTo(@NonNull Book o) {
        if (getTimeInserted() == null)
            return -1;
        if (o.getTimeInserted() == null)
            return 1;
        long diff = getTimeInserted() - o.getTimeInserted();
        if (diff == 0)
            return 0;
        if (diff > 0)
            return 1;
        else
            return -1;
    }

    public String getTags() {
        return mSearchTags;
    }

    public void setTags(String searchTags) {
        this.mSearchTags = searchTags;
    }
}
