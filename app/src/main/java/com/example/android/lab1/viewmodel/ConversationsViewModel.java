package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.chatmodels.Conversation;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataRealtimeDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ConversationsViewModel extends ViewModel {

    private static DatabaseReference CONVERSATIONS_REF;

    private FirebaseQueryLiveDataRealtimeDB liveData;

    private final MediatorLiveData<DataSnapshot> conversationsLiveData = new MediatorLiveData<>();

    public ConversationsViewModel(String bookID){
        CONVERSATIONS_REF = FirebaseDatabase.getInstance().getReference().child("conversations").child(bookID);
        liveData = new FirebaseQueryLiveDataRealtimeDB(CONVERSATIONS_REF);
        fetchData();
    }

    @NonNull
    public LiveData<DataSnapshot> getSnapshotLiveData(){
        return conversationsLiveData;
    }

    public void fetchData(){
        conversationsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            conversationsLiveData.postValue(dataSnapshot);
                        }
                    }).start();
                }else{
                    conversationsLiveData.setValue(null);
                }
            }
        });
    }
}
