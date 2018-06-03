package com.example.android.lab1.adapter;

public interface AddressResultReciever {

    /**
     * returns location -> address and position if location was passed from an array
     */
    void onPositionResolved(String address, boolean isCity, int position);

}
