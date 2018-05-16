package com.example.android.lab1.ui.searchbooks;

import com.example.android.lab1.model.Book;

public class BookSearchItem extends Book{

    private String userImage;
    private double userRating;
    private String bookID;

    public BookSearchItem() {}

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }
}
