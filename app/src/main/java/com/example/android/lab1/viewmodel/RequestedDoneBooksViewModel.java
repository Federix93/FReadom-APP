package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.firebaseutils.FirebaseDocumentSnapshotLiveDataFirestore;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RequestedDoneBooksViewModel extends ViewModel {

    private static final CollectionReference REQ_DONE_BOOKS_REF = FirebaseFirestore.getInstance()
            .collection("requestsDone").document(FirebaseAuth.getInstance().getUid()).collection("books");

    private final FirebaseQueryLiveDataFirestore liveData = new FirebaseQueryLiveDataFirestore(REQ_DONE_BOOKS_REF);

    private final MediatorLiveData<List<Book>> reqDoneBooksLiveData = new MediatorLiveData<>();

    public RequestedDoneBooksViewModel() {
        reqDoneBooksLiveData.addSource(liveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            reqDoneBooksLiveData.postValue(queryDocumentSnapshots.toObjects(Book.class));
                        }
                    }).start();
                }
                else
                    reqDoneBooksLiveData.setValue(null);
            }
        });
    }

    @NonNull
    public LiveData<List<Book>> getSnapshotLiveData() {
        return reqDoneBooksLiveData;
    }
}

