package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BorrowedBooks;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class BorrowedBooksViewModel extends ViewModel {

    Query BORROWED = null;

    private FirebaseQueryLiveDataFirestore liveData = null;

    private final MediatorLiveData<List<Book>> borrowedBooksLiveData = new MediatorLiveData<>();

    public BorrowedBooksViewModel(){
        BORROWED = FirebaseFirestore.getInstance()
                .collection("loans").whereEqualTo("lentTo", FirebaseAuth.getInstance().getUid());
        liveData = new FirebaseQueryLiveDataFirestore(BORROWED);
        borrowedBooksLiveData.addSource(liveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            borrowedBooksLiveData.postValue(queryDocumentSnapshots.toObjects(Book.class));
                        }
                    }).start();
                }
                else
                    borrowedBooksLiveData.setValue(null);
            }
        });
    }

    @NonNull
    public LiveData<List<Book>> getSnapshotLiveData(){
        return borrowedBooksLiveData;
    }
}
