package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Book implements Parcelable
{
    private String     isbn;
    private String     title;
    private String     author;
    private String     publisher;
    private int        publishYear;
    private Condition  conditions;
    private String     address;

    private String thumbnail;
    private List<String> userPhotosPath;

    public Book(String isbn,
                String title,
                String author,
                String publisher,
                int publishYear,
                Condition conditions,
                String address) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.conditions = conditions;
        this.address = address;
        thumbnail = null;
        userPhotosPath = new ArrayList<>();
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }


    public List<String> getUserPhotosPath() {
        return userPhotosPath;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public Condition getConditions() {
        return conditions;
    }

    public void setConditions(Condition conditions) {
        this.conditions = conditions;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                isbn,
                title,
                author,
                publisher,
                Integer.toString(publishYear),
                conditions.getStatus(),
                address
        });
        if (thumbnail != null)
            dest.writeValue(thumbnail);
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
            String[] stringFields = new String[7];
            source.readStringArray(stringFields);
            Book res = new Book(stringFields[0],
                    stringFields[1],
                    stringFields[2],
                    stringFields[3],
                    Integer.parseInt(stringFields[4]),
                    new Condition(stringFields[5]),
                    stringFields[6]);
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
