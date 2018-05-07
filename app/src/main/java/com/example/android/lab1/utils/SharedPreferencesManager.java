package com.example.android.lab1.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.lab1.R;

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

    public void setFirstRun(Boolean isFirstRun){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ADDRESS_KEY, isFirstRun);
        editor.apply();
    }

    public Boolean isFirstRun(){
        return mSharedPreferences.getBoolean(FIRST_RUN_KEY, true);
    }
}
