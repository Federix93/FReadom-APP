package com.example.android.lab1;

import android.app.Application;

import com.example.android.lab1.utils.LifecycleHandler;
import com.google.firebase.database.FirebaseDatabase;

public class FreadomApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        registerActivityLifecycleCallbacks(new LifecycleHandler());
    }
}
