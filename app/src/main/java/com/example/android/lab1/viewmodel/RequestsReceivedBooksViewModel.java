package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RequestsReceivedBooksViewModel extends ViewModel {

    private static final CollectionReference REQ_RECEIVED_BOOKS_REF = FirebaseFirestore.getInstance()
            .collection("requestsReceived").document(FirebaseAuth.getInstance().getUid()).collection("books");

    private final FirebaseQueryLiveDataFirestore liveData = new FirebaseQueryLiveDataFirestore(REQ_RECEIVED_BOOKS_REF);

    private final MediatorLiveData<List<Book>> reqReceivedLiveData = new MediatorLiveData<>();

    public RequestsReceivedBooksViewModel() {
        reqReceivedLiveData.addSource(liveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            reqReceivedLiveData.postValue(queryDocumentSnapshots.toObjects(Book.class));
                        }
                    }).start();
                }
                else
                    reqReceivedLiveData.setValue(null);
            }
        });
    }

    @NonNull
    public LiveData<List<Book>> getSnapshotLiveData() {
        return reqReceivedLiveData;
    }
}
