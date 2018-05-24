package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class RequestsReceived {

    public RequestsReceived(){}

    private List<String> mBooksID;

    public RequestsReceived(List<String> bookID){
        mBooksID = bookID;
    }
    public List<String> getBooksID() {
        return mBooksID;
    }

    public void setBooksID(ArrayList<String> mBooksID) {
        this.mBooksID = mBooksID;
    }
}
