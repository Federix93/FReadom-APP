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

    private final MediatorLiveData<String> chatIDLiveData = new MediatorLiveData<>();

    public OpenedChatViewModel(String bookID, String bookOwnerID){
        OPENED_CHATS = FirebaseDatabase.getInstance().getReference("openedChats")
                .child(bookID)
                .child(bookOwnerID)
                .child(FirebaseAuth.getInstance().getUid());
        OPENED_CHATS.keepSynced(true);
        liveData = new FirebaseQueryLiveDataRealtimeDB(OPENED_CHATS);
        fetchData();
    }

    public OpenedChatViewModel(){}

    private void fetchData(){
        openedChatsLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {
                    if (!dataSnapshot.exists()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                openedChatsLiveData.postValue(false);
                            }
                        }).start();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                openedChatsLiveData.postValue(true);
                            }
                        }).start();
                    }
                }
                else
                    openedChatsLiveData.setValue(null);
            }
        });
    }

    @NonNull
    public LiveData<Boolean> getSnapshotLiveData(){
        return openedChatsLiveData;
    }

    public LiveData<String> getChatID(){
        chatIDLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            chatIDLiveData.postValue((String)dataSnapshot.getValue());
                        }
                    }).start();
                }else{
                    chatIDLiveData.setValue(null);
                }
            }
        });
        return chatIDLiveData;
    }
}
