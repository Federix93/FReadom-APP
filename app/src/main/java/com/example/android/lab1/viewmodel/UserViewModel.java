package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.firebaseutils.FirebaseDocumentSnapshotLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends ViewModel {

    private static DocumentReference USER_REF;

    private String userID = null;

    private FirebaseDocumentSnapshotLiveDataFirestore liveData;

    private final MediatorLiveData<User> userLiveData = new MediatorLiveData<>();

    public UserViewModel(){
        USER_REF = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid());
        liveData = new FirebaseDocumentSnapshotLiveDataFirestore(USER_REF);
        fetchData();
    }

    public UserViewModel(String uid){
        USER_REF = FirebaseFirestore.getInstance().collection("users").document(uid);
        liveData = new FirebaseDocumentSnapshotLiveDataFirestore(USER_REF);
        fetchData();
    }

    @NonNull
    public LiveData<User> getSnapshotLiveData(){
        return userLiveData;
    }

    private void fetchData(){
        userLiveData.addSource(liveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot snapshot) {
                if(snapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            User user = snapshot.toObject(User.class);
                            user.setUserId(snapshot.getId());
                            userLiveData.postValue(user);
                        }
                    }).start();
                }else{
                    userLiveData.setValue(null);
                }
            }
        });
    }

}
