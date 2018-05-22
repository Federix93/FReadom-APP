package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ProfileBookAdapter;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.adapter.RecyclerBorrowedBooksAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BooksBorrowed;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class DashboardFragmentBorrowedItem extends Fragment {

    private RecyclerView mRecyclerView;
    FirebaseFirestore mFirebaseFirestore;
    List<Book> listBooks;

    public DashboardFragmentBorrowedItem() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_borrowed, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_books);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        listBooks = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirebaseFirestore.setFirestoreSettings(settings);
        mFirebaseFirestore.collection("borrowedBooks").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null)
                    return;
                if(queryDocumentSnapshots != null) {
                    List<BooksBorrowed> listBorrowedBooks = queryDocumentSnapshots.toObjects(BooksBorrowed.class);
                    for(BooksBorrowed booksBorrowed : listBorrowedBooks){
                        if(booksBorrowed.getBorrowerUserID().equals(mFirebaseAuth.getUid())) {
                            mFirebaseFirestore.collection("books").document(booksBorrowed.getBookID()).addSnapshotListener(
                                    new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                            if (snapshot != null && snapshot.exists()) {
                                                Book book = snapshot.toObject(Book.class);
                                                listBooks.add(book);
                                                mRecyclerView.setAdapter(new RecyclerBorrowedBooksAdapter(listBooks));
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
        return view;
    }
}
