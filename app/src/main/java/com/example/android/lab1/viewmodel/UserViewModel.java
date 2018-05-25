package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.FirebaseDocumentSnapshotLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends ViewModel {

    private static final DocumentReference USER_REF = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid());

    private final FirebaseDocumentSnapshotLiveDataFirestore liveData = new FirebaseDocumentSnapshotLiveDataFirestore(USER_REF);

    private final MediatorLiveData<User> userLiveData = new MediatorLiveData<>();

    public UserViewModel(){
        userLiveData.addSource(liveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot snapshot) {
                if(snapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            userLiveData.postValue(snapshot.toObject(User.class));
                        }
                    }).start();
                }else{
                    userLiveData.setValue(null);
                }
            }
        });
    }

    @NonNull
    public LiveData<User> getSnapshotLiveData(){
        return userLiveData;
    }

}
