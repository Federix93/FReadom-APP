package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BooksBorrowed {

    private String mOwnerUserID;
    private String mBorrowerUserID;
    private long mStartDate;
    private long mEndDate;
    private String mBookID;
    private int mState;

    public BooksBorrowed(){}

    public BooksBorrowed(String owner, String borrower, String bookID, int state){
        mOwnerUserID = owner;
        mBorrowerUserID = borrower;
        mBookID = bookID;
        mState = state;
    }

    //private List<String> mBooksID;

    public String getOwnerUserID() {
        return mOwnerUserID;
    }

    public void setOwnerUserID(String ownerUserID) {
        this.mOwnerUserID = ownerUserID;
    }

    public String getBorrowerUserID() {
        return mBorrowerUserID;
    }

    public void setBorrowerUserID(String borrowerUserID) {
        this.mBorrowerUserID = borrowerUserID;
    }

    public long getStartDate() {
        return mStartDate;
    }

    public void setStartDate(long startDate) {
        this.mStartDate = startDate;
    }

    public long getEndDate() {
        return mEndDate;
    }

    public void setEndDate(long endDate) {
        this.mEndDate = endDate;
    }

    public String getBookID() {
        return mBookID;
    }

    public void setBookID(String bookID) {
        this.mBookID = bookID;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        this.mState = state;
    }

    /*public BooksBorrowed(List<String> bookID){
        mBooksID = bookID;
    }
    public List<String> getBooksID() {
        return mBooksID;
    }

    public void setBooksID(ArrayList<String> mBooksID) {
        this.mBooksID = mBooksID;
    }*/
}
