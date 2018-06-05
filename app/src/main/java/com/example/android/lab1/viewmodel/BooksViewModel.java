package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.utils.firebaseutils.FirebaseQueryLiveDataFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BooksViewModel extends ViewModel {

    private FirebaseQueryLiveDataFirestore liveFirstRecyclerView;
    private FirebaseQueryLiveDataFirestore liveSecondRecyclerView;
    private final MediatorLiveData<List<Book>> booksFirstRecyclerLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Book>> booksSecondRecyclerLiveData = new MediatorLiveData<>();

    private int mLimit;

    public BooksViewModel(GeoPoint geoPoint, int firstDistance, int secondDistance, int limit) {
        mLimit = limit;
        GeoPoint[] firstGeoPoints = Utilities.buildBoundingBox(geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                (double) firstDistance);
        Query query1 = FirebaseFirestore.getInstance()
                .collection("books")
                .whereGreaterThan("geoPoint", firstGeoPoints[0])
                .whereLessThan("geoPoint", firstGeoPoints[1])
                .whereEqualTo("lentTo", null);
        liveFirstRecyclerView = new FirebaseQueryLiveDataFirestore(query1);
        fetchBooksOnFirstRecycler();
        GeoPoint[] secondGeoPoints = Utilities.buildBoundingBox(geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                (double) secondDistance);
        Query query2 = FirebaseFirestore.getInstance()
                .collection("books")
                .whereGreaterThan("geoPoint", secondGeoPoints[0])
                .whereLessThan("geoPoint", secondGeoPoints[1])
                .whereEqualTo("lentTo", null);
        liveSecondRecyclerView = new FirebaseQueryLiveDataFirestore(query2);
        fetchBooksOnSecondRecycler();
    }


    private void fetchBooksOnFirstRecycler() {
        booksFirstRecyclerLiveData.addSource(liveFirstRecyclerView, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<Book> books = new ArrayList<>();
                            int valid = 0;
                            String uid = FirebaseAuth.getInstance().getUid();
                            for(Book b : queryDocumentSnapshots.toObjects(Book.class)){
                                if (uid == null || !b.getUid().equals(uid)) {
                                    books.add(b);
                                    valid++;
                                }
                                if (valid > mLimit) {
                                    break;
                                }
                            }
                            booksFirstRecyclerLiveData.postValue(books);
                        }
                    }).start();
                }else{
                    booksFirstRecyclerLiveData.setValue(null);
                }
            }
        });
    }

    private void fetchBooksOnSecondRecycler(){
        booksSecondRecyclerLiveData.addSource(liveSecondRecyclerView, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<Book> books = new ArrayList<>();
                            int valid = 0;
                            String uid = FirebaseAuth.getInstance().getUid();
                            for(Book b : queryDocumentSnapshots.toObjects(Book.class)){
                                if (uid == null || !b.getUid().equals(uid)) {
                                    books.add(b);
                                    valid++;
                                }
                                if (valid > mLimit) {
                                    break;
                                }
                            }
                            Book temp;
                            for (int i = 0; i < books.size() - 1; i++) {
                                for (int i1 = i + 1; i1 < books.size(); i1++) {
                                    if (books.get(i).getTimeInserted().getTime() <
                                            books.get(i1).getTimeInserted().getTime()) {
                                        temp = books.get(i);
                                        books.set(i, books.get(i1));
                                        books.set(i1, temp);
                                    }
                                }
                            }
                            booksSecondRecyclerLiveData.postValue(books);
                        }
                    }).start();
                }else{
                    booksSecondRecyclerLiveData.setValue(null);
                }
            }
        });
    }

    @NonNull
    public LiveData<List<Book>> getBooksFirstRecycler(){
        return booksFirstRecyclerLiveData;
    }
    @NonNull
    public LiveData<List<Book>> getBooksSecondRecycler(){
        return booksSecondRecyclerLiveData;
    }
}