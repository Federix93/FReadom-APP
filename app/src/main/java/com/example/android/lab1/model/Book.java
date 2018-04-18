package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Book implements Parcelable
{
    private String mIsbn;
    private String mTitle;
    private String mAuthor;
    private String mPublisher;
    private int    mPublishYear;
    //private Condition mConditions;
    private String mAddress;
    private String mUid;
    private String mThumbnail;
    private List<String> mUserPhotosPath;

    public Book(){}

    public Book(String mIsbn, String title, String author, String publisher, int publishYear,
                /*Condition conditions,*/ String address, String Uid) {
        mIsbn = mIsbn;
        mTitle = title;
        mAuthor = author;
        mPublisher = publisher;
        mPublishYear = publishYear;
        //mConditions = conditions;
        mAddress = address;
        mThumbnail = null;
        mUserPhotosPath = new ArrayList<>();
        mUid = Uid;
    }

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

    /*public Condition getConditions() {
        return mConditions;
    }

    public void setConditions(Condition conditions) {
        mConditions = conditions;
    }*/

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                mIsbn,
                mTitle,
                mAuthor,
                mPublisher,
                Integer.toString(mPublishYear),
                /*mConditions.getStatus(),*/
                mAddress,
                mUid
        });
        if (mThumbnail != null)
            dest.writeValue(mThumbnail);
        if (getUserPhotosPath() != null && getUserPhotosPath().size() > 0) {
            dest.writeInt(getUserPhotosPath().size());
            for (String s : getUserPhotosPath())
                dest.writeString(s);
        }
        else
        {
            dest.writeInt(0);
        }

    }

    public static final Creator CREATOR = new Creator(){

        @Override
        public Book createFromParcel(Parcel source) {
            String[] stringFields = new String[8];
            source.readStringArray(stringFields);
            Book res = new Book(stringFields[0],
                    stringFields[1],
                    stringFields[2],
                    stringFields[3],
                    Integer.parseInt(stringFields[4]),
                    /*new Condition(stringFields[5]),*/
                    stringFields[6], stringFields[7]);
            String webThumbnail = source.readString();
            if (webThumbnail != null)
                res.setThumbnail(webThumbnail);
            Integer numOfBooks = source.readInt();
            if (numOfBooks > 0)
            {
                for (int i = 0; i < numOfBooks; i++)
                {
                    res.getUserPhotosPath().add(source.readString());
                }
            }

            return res;
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
