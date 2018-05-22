package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BorrowedBooks {

    public BorrowedBooks(){}

    private List<String> mBooksID;

    public BorrowedBooks(List<String> bookID){
        mBooksID = bookID;
    }
    public List<String> getBooksID() {
        return mBooksID;
    }

    public void setBooksID(ArrayList<String> mBooksID) {
        this.mBooksID = mBooksID;
    }
}
