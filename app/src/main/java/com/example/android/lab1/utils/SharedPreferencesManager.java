package com.example.android.lab1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.android.lab1.R;
import com.google.firebase.firestore.GeoPoint;

/**
 * Created by Federico on 20/03/2018.
 */

public class SharedPreferencesManager {

    private static SharedPreferencesManager mInstance = null;
    private static SharedPreferences mSharedPreferences = null;
    private static Context mContext;

    private final String IMAGE_KEY = "Image";
    private final String ADDRESS_KEY = "Address";
    private final String FIRST_RUN_KEY = "FirstRun";
    private final String USER_LAT = "USER_LAT";
    private final String USER_LON = "USER_LON";

    private SharedPreferencesManager(){}

    public static SharedPreferencesManager getInstance(Context pContext){
        mContext = pContext;
        mSharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.shared_preferences), mContext.MODE_PRIVATE);
        if (mInstance == null)
            mInstance = new SharedPreferencesManager();
        return mInstance;
    }

    public void putImage(String image){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(IMAGE_KEY, image);
        editor.apply();
    }

    public String getImage(){
        return mSharedPreferences.getString(IMAGE_KEY, null);
    }

    public void putAddress(String address){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ADDRESS_KEY, address);
        editor.apply();
    }

    public String getAddress(){
        return mSharedPreferences.getString(ADDRESS_KEY, mContext.getResources().getString(R.string.selection_position));
    }

    public void putFirstRun(Boolean isFirstRun){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(FIRST_RUN_KEY, isFirstRun);
        editor.apply();
    }

    public Boolean isFirstRun(){
        return mSharedPreferences.getBoolean(FIRST_RUN_KEY, true);
    }

    public GeoPoint getPosition() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                mContext.getString(R.string.position_shared_preference_file),
                Context.MODE_PRIVATE);
        if (sharedPreferences.contains(USER_LAT) &&
                sharedPreferences.contains(USER_LON))
            return new GeoPoint(sharedPreferences.getFloat(USER_LAT, 0f),
                    sharedPreferences.getFloat(USER_LON, 0f));
        return null;
    }

    public void setPosition(@NonNull GeoPoint mCurrentPosition) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.position_shared_preference_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(USER_LAT, (float) mCurrentPosition.getLatitude());
        editor.putFloat(USER_LON, (float) mCurrentPosition.getLongitude());
        editor.apply();
    }
}
