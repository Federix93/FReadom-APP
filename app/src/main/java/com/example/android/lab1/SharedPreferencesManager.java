package com.example.android.lab1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by Federico on 20/03/2018.
 */

public class SharedPreferencesManager {

    private static SharedPreferencesManager mInstance = null;
    private static SharedPreferences mSharedPreferences = null;
    private static Context mContext;

    private final String IMAGE_KEY = "Image";

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
        editor.putString("Image", image);
        editor.apply();
    }

    public String getImage(){
        return mSharedPreferences.getString(IMAGE_KEY, null);
    }
}
