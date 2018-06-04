package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.chatmodels.Message;
import com.example.android.lab1.utils.firebaseutils.FirebaseChildLiveDataRealtimeDB;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataRealtimeDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MessagesViewModel extends ViewModel {

    private static DatabaseReference MESSAGES;

    private FirebaseQueryLiveDataRealtimeDB liveData;

    private final MediatorLiveData<List<Message>> messagesLiveData = new MediatorLiveData<>();

    public MessagesViewModel(String chatID){
        if (chatID != null) {
            MESSAGES = FirebaseDatabase.getInstance().getReference("messages").child(chatID);
            liveData = new FirebaseQueryLiveDataRealtimeDB(MESSAGES);
            messagesLiveData.addSource(liveData, new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<Message> messagesList = new ArrayList<>();
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    messagesList.add(d.getValue(Message.class));
                                }
                                messagesLiveData.postValue(messagesList);
                            }
                        }).start();
                    } else {
                        messagesLiveData.setValue(null);
                    }
                }
            });
        }
    }


    @NonNull
    public LiveData<List<Message>> getSnapshotLiveData(){
        return messagesLiveData;
    }
}
