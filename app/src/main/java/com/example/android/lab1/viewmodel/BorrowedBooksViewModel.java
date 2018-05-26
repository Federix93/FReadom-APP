package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BorrowedBooks;
import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.FirebaseDocumentSnapshotLiveDataFirestore;
import com.example.android.lab1.utils.FirebaseQueryLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class BorrowedBooksViewModel extends ViewModel {
    private static final DocumentReference BORROWED_BOOKS_REF = FirebaseFirestore.getInstance()
            .collection("borroweBooks")
            .document(FirebaseAuth.getInstance().getUid());

    private final FirebaseDocumentSnapshotLiveDataFirestore liveData = new FirebaseDocumentSnapshotLiveDataFirestore(BORROWED_BOOKS_REF);

    private final MediatorLiveData<BorrowedBooks> borrowedBooksLiveData = new MediatorLiveData<>();

    public BorrowedBooksViewModel(){
        borrowedBooksLiveData.addSource(liveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot snapshot) {
                if(snapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            borrowedBooksLiveData.postValue(snapshot.toObject(BorrowedBooks.class));
                        }
                    }).start();
                }else{
                    borrowedBooksLiveData.setValue(null);
                }
            }
        });
    }

    @NonNull
    public LiveData<BorrowedBooks> getSnapshotLiveData(){
        return borrowedBooksLiveData;
    }
}
