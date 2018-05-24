package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBorrowedBooksAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BorrowedBooks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RequestsFragmentDoneItem extends Fragment {

    private RecyclerView mRecyclerView;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    List<Book> listBooks;
    List<String> booksID;
    List<String> chatIDs;
    List<String> usersID;
    DatabaseReference openedChatReference;
    RecyclerBorrowedBooksAdapter mAdapter;

    public void RequestsFragmentDoneItem() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        listBooks = new ArrayList<>();
        chatIDs = new ArrayList<>();
        usersID = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerBorrowedBooksAdapter(listBooks, booksID, chatIDs, usersID);
        mRecyclerView.setAdapter(mAdapter);

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirebaseFirestore.setFirestoreSettings(settings);

        firebaseDatabase = FirebaseDatabase.getInstance();
        openedChatReference = firebaseDatabase.getReference().child("openedChats");

        if (mFirebaseAuth.getUid() != null) {
            mFirebaseFirestore.collection("borrowedBooks").document(mFirebaseAuth.getUid())
                    .addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if (snapshot != null && snapshot.exists()) {
                                BorrowedBooks booksBorrowed = snapshot.toObject(BorrowedBooks.class);
                                final List<String> booksID = booksBorrowed.getBooksID();

                                for (final String bookID : booksID) {
                                    mFirebaseFirestore.collection("books").document(bookID).addSnapshotListener(
                                            new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                                    if (snapshot != null && snapshot.exists()) {
                                                        final Book book = snapshot.toObject(Book.class);
                                                        openedChatReference.child(bookID)
                                                                .child(book.getUid())
                                                                .child(mFirebaseAuth.getUid())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String chatID = (String) dataSnapshot.getValue();
                                                                        chatIDs.add(chatID);
                                                                        listBooks.add(book);
                                                                        String userId = book.getUid();
                                                                        usersID.add(userId);
                                                                        mAdapter.setItems(listBooks, booksID, chatIDs, usersID);
                                                                        mAdapter.notifyDataSetChanged();
                                                                    }
                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    });
        }
        return view;
    }
}
