package com.example.android.lab1;

import android.net.Uri;

public class UserManager {

    private static User instance = User.getInstance();

    private UserManager(){}

    public static User getUser(){
        return instance;
    }

}
