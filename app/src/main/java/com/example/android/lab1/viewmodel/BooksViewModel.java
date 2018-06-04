package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    private static Query BOOK_REF_1;
    private static Query BOOK_REF_2;

    private FirebaseQueryLiveDataFirestore liveData;
    private FirebaseQueryLiveDataFirestore liveFirstRecyclerView;
    private FirebaseQueryLiveDataFirestore liveSecondRecyclerView;
    private final MediatorLiveData<List<Book>> bookLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Book>> booksFirstRecyclerLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Book>> booksSecondRecyclerLiveData = new MediatorLiveData<>();

    public BooksViewModel(String uid){
        BOOK_REF_1 = FirebaseFirestore.getInstance().collection("books").whereEqualTo("uid", uid);
        liveData = new FirebaseQueryLiveDataFirestore(BOOK_REF_1);
        fetchData();
    }

    public BooksViewModel(){

    }

    public BooksViewModel(GeoPoint geoPoint){

        GeoPoint[] firstGeoPoints = Utilities.buildBoundingBox(geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                (double)15000);
        BOOK_REF_1 = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", firstGeoPoints[0])
                .whereLessThan("geoPoint", firstGeoPoints[1]).limit(30);
        liveFirstRecyclerView = new FirebaseQueryLiveDataFirestore(BOOK_REF_1);
        fetchBooksOnFirstRecycler();
        GeoPoint[] secondGeoPoints = Utilities.buildBoundingBox(geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                (double) 15000);
        BOOK_REF_2 = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", secondGeoPoints[0])
                .whereLessThan("geoPoint", secondGeoPoints[1]).limit(30);
        liveSecondRecyclerView = new FirebaseQueryLiveDataFirestore(BOOK_REF_2);
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
                            for(Book b : queryDocumentSnapshots.toObjects(Book.class)){
                                if(!b.getUid().equals(FirebaseAuth.getInstance().getUid()) && b.getLentTo() == null){
                                    books.add(b);
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
                            for(Book b : queryDocumentSnapshots.toObjects(Book.class)){
                                if(!b.getUid().equals(FirebaseAuth.getInstance().getUid()) && b.getLentTo() == null){
                                    books.add(b);
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
    public LiveData<List<Book>> getSnapshotLiveData(){
        return bookLiveData;
    }
    @NonNull
    public LiveData<List<Book>> getBooksFirstRecycler(){
        return booksFirstRecyclerLiveData;
    }
    @NonNull
    public LiveData<List<Book>> getBooksSecondRecycler(){
        return booksSecondRecyclerLiveData;
    }


    private void fetchData(){
        bookLiveData.addSource(liveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable final QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            bookLiveData.postValue(queryDocumentSnapshots.toObjects(Book.class));
                        }
                    }).start();
                }else{
                    bookLiveData.setValue(null);
                }
            }
        });
    }
}