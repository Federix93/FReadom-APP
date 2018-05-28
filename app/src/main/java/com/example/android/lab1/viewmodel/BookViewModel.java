package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.firebaseutils.FirebaseDocumentSnapshotLiveDataFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookViewModel extends ViewModel {

    private String mBookID;

    private static DocumentReference SPECIFIC_BOOK_REF;

    private FirebaseDocumentSnapshotLiveDataFirestore liveData;

    private final MediatorLiveData<Book> bookLiveData = new MediatorLiveData<>();

    public BookViewModel(String bookID){
        mBookID = bookID;
        SPECIFIC_BOOK_REF = FirebaseFirestore.getInstance().collection("books").document(mBookID);
        liveData = new FirebaseDocumentSnapshotLiveDataFirestore(SPECIFIC_BOOK_REF);
        fetchData();
    }

    private void fetchData(){
        bookLiveData.addSource(liveData, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable final DocumentSnapshot snapshot) {
                if(snapshot != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            bookLiveData.postValue(snapshot.toObject(Book.class));
                        }
                    }).start();
                }else{
                    bookLiveData.setValue(null);
                }
            }
        });
    }

    @NonNull
    public LiveData<Book> getSnapshotLiveData(){
        return bookLiveData;
    }

}
