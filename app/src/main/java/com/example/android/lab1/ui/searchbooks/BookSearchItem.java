package com.example.android.lab1.ui.searchbooks;

import com.example.android.lab1.model.Book;

import java.util.Comparator;

public class BookSearchItem extends Book {

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

    public static Comparator<BookSearchItem> conditionsComp = new Comparator<BookSearchItem>() {

        public int compare(BookSearchItem b1, BookSearchItem b2) {
            int cond1 = b1.getCondition();
            int cond2 = b2.getCondition();

            return cond2-cond1;
        }};

    public static Comparator<BookSearchItem> ratingComp = new Comparator<BookSearchItem>() {

        public int compare(BookSearchItem b1, BookSearchItem b2) {
            double rat1 = b1.getUserRating();
            double rat2 = b2.getCondition();

            if (rat1 < rat2) return 1;
            if (rat1 > rat2) return -1;
            return 0;
        }};
}
