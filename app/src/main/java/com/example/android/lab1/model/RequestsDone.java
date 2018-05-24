package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class RequestsDone {

    public RequestsDone(){}

    private List<String> mBooksID;

    public RequestsDone(List<String> bookID){
        mBooksID = bookID;
    }
    public List<String> getBooksID() {
        return mBooksID;
    }

    public void setBooksID(ArrayList<String> mBooksID) {
        this.mBooksID = mBooksID;
    }
}
