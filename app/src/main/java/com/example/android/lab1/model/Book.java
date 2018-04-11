package com.example.android.lab1.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Book implements Parcelable
{
    private String     isbn;
    private String     title;
    private String     author;
    private String     publisher;
    private int        publishYear;
    private Condition  conditions;
    private LatLng     position;

    private String thumbnail;
    private List<String> userPhotosPath;

    public Book(String isbn, String title, String author, String publisher, int publishYear, Condition conditions, LatLng position) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.conditions = conditions;
        this.position = position;
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

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
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
                conditions.getStatus()
        });
        dest.writeParcelable(position, flags);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator(){

        @Override
        public Book createFromParcel(Parcel source) {
            String[] stringFields = new String[6];
            source.readStringArray(stringFields);
            LatLng latLng = source.readParcelable(LatLng.class.getClassLoader());
            return new Book(stringFields[0],
                    stringFields[1],
                    stringFields[2],
                    stringFields[3],
                    Integer.parseInt(stringFields[4]),
                    new Condition(stringFields[5]),
                    latLng);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
