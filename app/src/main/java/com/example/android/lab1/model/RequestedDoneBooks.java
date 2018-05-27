package com.example.android.lab1.model;

import java.util.Map;

public class RequestedDoneBooks {

    public  RequestedDoneBooks(){}

    private Map<String, Book> mMapRequestedDone;

    public Map<String, Book> getMapRequestedDone() {
        return mMapRequestedDone;
    }

    public void setMapRequestedDone(Map<String, Book> mapRequestedDone) {
        this.mMapRequestedDone = mapRequestedDone;
    }
}
