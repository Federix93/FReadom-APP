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

    private final String USER_KEY = "User";

    private SharedPreferencesManager(){}

    public static SharedPreferencesManager getInstance(Context pContext){
        mContext = pContext;
        mSharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.shared_preferences), mContext.MODE_PRIVATE);
        if (mInstance == null)
            mInstance = new SharedPreferencesManager();
        return mInstance;
    }

    public void putUser(User user){
        Gson gson = new Gson();
        String json = gson.toJson(user);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("User", json);
        editor.apply();
    }

    public User getUser(){
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(USER_KEY, null);
        return gson.fromJson(json, User.class);
    }
}
