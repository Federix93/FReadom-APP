package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.lang.reflect.Field;
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
    private Integer mCondition;
    private String mAddress;
    private GeoPoint mGeoPoint;
    private String mUid; // user id
    private String mWebThumbnail;
    private ArrayList<String> mBookPhotosPaths;
    private String mSearchTags;
    private Long mLoanStart;
    private Long mLoanEnd;
    private Integer mGenre;
    private String mInfoLink;
    private @ServerTimestamp
    Date mTimeInserted;
    private String mDescription;
    private String mLentTo;
    private String mBookID;

    public Book(Book toBeCopied) {
        setTitle(toBeCopied.getTitle());
        setIsbn(toBeCopied.getIsbn());
        setAuthors(toBeCopied.getAuthors());
        setPublisher(toBeCopied.getPublisher());
        setPublishYear(toBeCopied.getPublishYear());
        setCondition(toBeCopied.getCondition());
        setAddress(toBeCopied.getAddress());
        setGeoPoint(toBeCopied.getGeoPoint());
        setUid(toBeCopied.getUid());
        setWebThumbnail(toBeCopied.getWebThumbnail());
        setUserBookPhotosStoragePath(new ArrayList<>(toBeCopied.getUserBookPhotosStoragePath()));
        setTags(toBeCopied.getTags());
        setLoanStart(toBeCopied.getLoanStart());
        setLoanEnd(toBeCopied.getLoanEnd());
        setGenre(toBeCopied.getGenre());
        setInfoLink(toBeCopied.getInfoLink());
        setTimeInserted(toBeCopied.getTimeInserted());
        setDescription(toBeCopied.getDescription());
        setLentTo(toBeCopied.getLentTo());
        setBookID(toBeCopied.getBookID());
    }

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

    @SuppressWarnings("unchecked")
    protected Book(Parcel in) {
        mDescription = in.readString();
        mIsbn = in.readString();
        mTitle = in.readString();
        mBookPhotosPaths = in.readArrayList(String.class.getClassLoader());
        mWebThumbnail = in.readString();
        mUid = in.readString();
        mAddress = in.readString();
        mPublisher = in.readString();
        mAuthors = in.readString();
        mGeoPoint = new GeoPoint(in.readDouble(), in.readDouble());
        mPublishYear = in.readInt();
        mLoanStart = in.readLong();
        mLoanEnd = in.readLong();
        mInfoLink = in.readString();
        Long time = in.readLong();
        if (time != DEFAULT_VALUE_INT) {
            Date d = new Date();
            d.setTime(time);
            mTimeInserted = d;
        } else
            mTimeInserted = null;
        mSearchTags = in.readString();
        mLentTo = (String) in.readValue(String.class.getClassLoader());
        mCondition = (Integer) in.readValue(Integer.class.getClassLoader());
        mGenre = (Integer) in.readValue(Integer.class.getClassLoader());
        mBookID = in.readString();
    }

    public Integer getCondition() {
        return mCondition;
    }

    public void setCondition(Integer condition) {
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

    public Integer getGenre() {
        return mGenre;
    }

    public void setGenre(Integer genre) {
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
        dest.writeValue(mLentTo);
        dest.writeValue(mCondition);
        dest.writeValue(mGenre);
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

    public void nullifyInvalidStrings() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(String.class)) {
                try {
                    String o = (String) field.get(this);
                    if (o != null && o.equals(""))
                        field.set(this, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getLentTo() {
        return mLentTo;
    }

    public void setLentTo(String mLentTo) {
        this.mLentTo = mLentTo;
    }
}