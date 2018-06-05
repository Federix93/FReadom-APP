package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class YourLibraryViewModel extends ViewModel {

    private final MediatorLiveData<List<Book>> bookLiveData = new MediatorLiveData<>();
    private FirebaseQueryLiveDataFirestore liveData;

    public YourLibraryViewModel(String uid) {
        Query query1 = FirebaseFirestore.getInstance()
                .collection("books")
                .whereEqualTo("uid", uid);
        liveData = new FirebaseQueryLiveDataFirestore(query1);
        fetchData();
    }


    @NonNull
    public LiveData<List<Book>> getSnapshotLiveData() {
        return bookLiveData;
    }


    private void fetchData() {
        bookLiveData.addSource(liveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            bookLiveData.postValue(queryDocumentSnapshots.toObjects(Book.class));
                        }
                    }).start();
                } else {
                    bookLiveData.setValue(null);
                }
            }
        });
    }

}
