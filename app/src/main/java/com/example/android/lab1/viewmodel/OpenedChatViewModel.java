package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataRealtimeDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;

public class OpenedChatViewModel extends ViewModel{

    private static DatabaseReference OPENED_CHATS;

    private FirebaseQueryLiveDataRealtimeDB liveData;

    private final MediatorLiveData<Boolean> openedChatsLiveData = new MediatorLiveData<>();

    public OpenedChatViewModel(String bookID, String bookOwnerID){
        OPENED_CHATS = FirebaseDatabase.getInstance().getReference("openedChats")
                .child(bookID)
                .child(bookOwnerID)
                .child(FirebaseAuth.getInstance().getUid());
        liveData = new FirebaseQueryLiveDataRealtimeDB(OPENED_CHATS);
        fetchData();
    }

    public OpenedChatViewModel(){}

    @NonNull
    public LiveData<Boolean> getSnapshotLiveData(){
        return openedChatsLiveData;
    }

    public void fetchData(){
        openedChatsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if(dataSnapshot == null || !dataSnapshot.exists()){
                    openedChatsLiveData.setValue(false);
                }else{
                    openedChatsLiveData.setValue(true);
                }
            }
        });
    }
}
