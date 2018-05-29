package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
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

    private final String DEFAULT_VALUE_STRING = null;
    private final int DEFAULT_VALUE_INT = -1;
    private final List DEFAULT_VALUE_LIST = new ArrayList();


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
    private @ServerTimestamp
    Date mTimeInserted;
    private String mDescription;
    private boolean mIsAlreadyLent;
    private String mBookID;

    public Book() {
    }

    public boolean isAlreadyLent() {
        return mIsAlreadyLent;
    }

    public void setIsAlreadyLent(boolean isAlreadyLent) {
        this.mIsAlreadyLent = isAlreadyLent;
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
        return null;
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

    public void setAuthors(String authors) {
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

    public String getBookID() {
        return mBookID;
    }

    public void setBookID(String bookID) {
        this.mBookID = bookID;
    }

    @SuppressWarnings("unchecked")
    protected Book(Parcel in) {
        mDescription = in.readString();
        mIsbn = in.readString();
        Log.d("LULLO", "ISBN ON READ: " + mIsbn);
        mTitle = in.readString();
        Log.d("LULLO", "Title ON READ: " + mTitle);
        mBookPhotosPaths = in.readArrayList(String.class.getClassLoader());
        Log.d("LULLO", "LIST ON READ: " + mBookPhotosPaths.size());
        mWebThumbnail = in.readString();
        Log.d("LULLO", "WTHUMB ON READ: " + mWebThumbnail);
        mUid = in.readString();
        Log.d("LULLO", "UID ON READ: " + mUid);
        mAddress = in.readString();
        Log.d("LULLO", "ADDRESS ON READ: " + mAddress);
        mPublisher = in.readString();
        Log.d("LULLO", "PUBLISHER ON READ: " + mPublisher);
        mAuthors = in.readString();
        Log.d("LULLO", "AUTHORS ON READ: " + mAuthors);
        mGeoPoint = new GeoPoint(in.readDouble(), in.readDouble());
        mPublishYear = in.readInt();
        Log.d("LULLO", "PUBLISHYEAR ON READ: " + mPublishYear);
        mLoanStart = in.readLong();
        Log.d("LULLO", "LOAN START ON READ: " + mLoanStart);
        mLoanEnd = in.readLong();
        Log.d("LULLO", "LOAN END ON READ: " + mLoanEnd);
        mInfoLink = in.readString();
        Log.d("LULLO", "INFO LINK ON READ: " + mInfoLink);
        Long time = in.readLong();
        if (time != DEFAULT_VALUE_INT) {
            Date d = new Date();
            d.setTime(time);
            mTimeInserted = d;
        } else
            mTimeInserted = null;
        Log.d("LULLO", "TimeINS ON READ: " + mTimeInserted);
        mSearchTags = in.readString();
        Log.d("LULLO", "TAGS ON READ: " + mSearchTags);
        mCondition = in.readInt();
        Log.d("LULLO", "CONDITION ON READ: " + mCondition);
        mGenre = in.readInt();
        Log.d("LULLO", "GENRE ON READ: " + mGenre);
        mBookID = in.readString();
        Log.d("LULLO", "BOOOKID ON READ: " + mBookID);
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
        if (mDescription != null)
            dest.writeString(mDescription);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mIsbn != null)
            dest.writeString(mIsbn);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mTitle != null)
            dest.writeString(mTitle);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mBookPhotosPaths != null && !mBookPhotosPaths.isEmpty())
            dest.writeList(mBookPhotosPaths);
        else
            dest.writeList(DEFAULT_VALUE_LIST);
        if (mWebThumbnail != null)
            dest.writeString(mWebThumbnail);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mUid != null)
            dest.writeString(mUid);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mAddress != null)
            dest.writeString(mAddress);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mPublisher != null)
            dest.writeString(mPublisher);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mAuthors != null)
            dest.writeString(mAuthors);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mGeoPoint != null) {
            dest.writeDouble(mGeoPoint.getLatitude());
            dest.writeDouble(mGeoPoint.getLongitude());
        }else{
            dest.writeDouble(DEFAULT_VALUE_INT);
            dest.writeDouble(DEFAULT_VALUE_INT);
        }
        if (mPublishYear != null)
            dest.writeInt(mPublishYear);
        else
            dest.writeInt(DEFAULT_VALUE_INT);
        if (mLoanStart != null)
            dest.writeLong(mLoanStart);
        else
            dest.writeLong(DEFAULT_VALUE_INT);
        if (mLoanEnd != null)
            dest.writeLong(mLoanEnd);
        else
            dest.writeLong(DEFAULT_VALUE_INT);
        if (mInfoLink != null)
            dest.writeString(mInfoLink);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        if (mTimeInserted != null)
            dest.writeLong(mTimeInserted.getTime());
        else
            dest.writeLong(DEFAULT_VALUE_INT);
        if (mSearchTags != null)
            dest.writeString(mSearchTags);
        else
            dest.writeString(DEFAULT_VALUE_STRING);
        dest.writeInt(mCondition);
        Log.d("LULLO", "CONDITION ON WRITE: " + mCondition);
        dest.writeInt(mGenre);
        dest.writeString(mBookID);

    }

    @ServerTimestamp
    public Date getTimeInserted() {
        return mTimeInserted;
    }

    public void setTimeInserted(Date mTimeInserted) {
        this.mTimeInserted = mTimeInserted;
    }

    @Override
    public int compareTo(@NonNull Book o) {
        if (getTimeInserted() == null)
            return -1;
        if (o.getTimeInserted() == null)
            return 1;
        long diff = getTimeInserted().getTime() - o.getTimeInserted().getTime();
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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }
}
