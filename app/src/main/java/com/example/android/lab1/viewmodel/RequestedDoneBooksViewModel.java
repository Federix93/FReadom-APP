package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.BorrowedBooks;
import com.example.android.lab1.model.RequestedDoneBooks;
import com.example.android.lab1.utils.firebaseutils.FirebaseDocumentSnapshotLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestedDoneBooksViewModel extends ViewModel {

    private static final DocumentReference REQ_DONE_BOOKS_REF = FirebaseFirestore.getInstance()
            .collection("requestedDone")
            .document(FirebaseAuth.getInstance().getUid());

    private final FirebaseDocumentSnapshotLiveDataFirestore liveData = new FirebaseDocumentSnapshotLiveDataFirestore(REQ_DONE_BOOKS_REF);

    private final MediatorLiveData<RequestedDoneBooks> reqDoneBooksLiveData = new MediatorLiveData<>();

    public RequestedDoneBooksViewModel(){
        reqDoneBooksLiveData.addSource(liveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot snapshot) {
                if(snapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            reqDoneBooksLiveData.postValue(snapshot.toObject(RequestedDoneBooks.class));
                        }
                    }).start();
                }else{
                    reqDoneBooksLiveData.setValue(null);
                }
            }
        });
    }

    @NonNull
    public LiveData<RequestedDoneBooks> getSnapshotLiveData(){
        return reqDoneBooksLiveData;
    }
}

