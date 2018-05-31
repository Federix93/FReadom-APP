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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatViewModel extends ViewModel {

    private static DatabaseReference MESSAGE;

    private FirebaseChildLiveDataRealtimeDB liveData;

    private final MediatorLiveData<Message> chatLiveData = new MediatorLiveData<>();

    private final MediatorLiveData<Message> messagesLiveData = new MediatorLiveData<>();

    public ChatViewModel(String chatID){
        MESSAGE = FirebaseDatabase.getInstance().getReference("messages").child(chatID);
        liveData = new FirebaseChildLiveDataRealtimeDB(MESSAGE);
        chatLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            chatLiveData.postValue(dataSnapshot.getValue(Message.class));
                        }
                    }).start();
                }else{
                    chatLiveData.setValue(null);
                }
            }
        });
    }

    public ChatViewModel(){}


    @NonNull
    public LiveData<Message> getSnapshotLiveData(){
        return chatLiveData;
    }

}
