package com.example.android.lab1;

import android.app.Application;

import com.example.android.lab1.utils.LifecycleHandler;

public class FreadomApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new LifecycleHandler());
    }
}
