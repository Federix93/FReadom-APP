package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataRealtimeDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserRealtimeDBViewModel extends ViewModel {

    private DatabaseReference USER_REF;

    private FirebaseQueryLiveDataRealtimeDB liveData;

    private final MediatorLiveData<User> usersLiveData = new MediatorLiveData<>();

    UserRealtimeDBViewModel(String uid){
        USER_REF = FirebaseDatabase.getInstance().getReference("users").child(uid);
        liveData = new FirebaseQueryLiveDataRealtimeDB(USER_REF);
        fetchData();
    }

    @NonNull
    public LiveData<User> getSnapshotLiveData(){
        return usersLiveData;
    }

    public void fetchData(){
        usersLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            usersLiveData.postValue(dataSnapshot.getValue(User.class));
                        }
                    }).start();
                }else{
                    usersLiveData.setValue(null);
                }
            }
        });
    }
}
