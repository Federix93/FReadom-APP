package com.example.android.lab1.ui.homepage;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerFragmentBooksAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.viewmodel.RequestedDoneBooksViewModel;
import com.example.android.lab1.viewmodel.UserRealtimeDBViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragmentDoneItem extends Fragment {

    private RecyclerView mRecyclerView;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    List<Book> listBooks;
    List<String> chatIDs;
    List<User> mUsersOwner;
    DatabaseReference openedChatReference;
    RecyclerFragmentBooksAdapter mAdapter;

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
        mUsersOwner = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerFragmentBooksAdapter(getActivity(), listBooks,  mUsersOwner);

        //mAdapter = new RecyclerBorrowedBooksAdapter(listBooks, booksID, chatIDs, usersID);
        mRecyclerView.setAdapter(mAdapter);

        /*final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirebaseFirestore.setFirestoreSettings(settings);

        firebaseDatabase = FirebaseDatabase.getInstance();
        openedChatReference = firebaseDatabase.getReference().child("openedChats");
*/
        RequestedDoneBooksViewModel requestedDoneBooksViewModel = ViewModelProviders.of(getActivity()).get(RequestedDoneBooksViewModel.class);
        requestedDoneBooksViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<List<Book>>() {
            @Override
            public void onChanged(@Nullable List<Book> books) {
                if(books != null) {
                    for (final Book b : books) {
                        FirebaseDatabase.getInstance().getReference("users").child(b.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                listBooks.add(b);
                                mUsersOwner.add(dataSnapshot.getValue(User.class));
                                mAdapter.setItems(listBooks, mUsersOwner);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        /*UserRealtimeDBViewModel userRealtimeDBViewModel = ViewModelProviders.of(getActivity(), new ViewModelFactory(b.getUid())).get(UserRealtimeDBViewModel.class);
                        userRealtimeDBViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<User>() {
                            @Override
                            public void onChanged(@Nullable User user) {
                                if (user != null) {
                                    mUsersOwner.add(user);
                                    mAdapter.setItems(listBooks, mUsersOwner);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });*/
                    }
                }
            }
        });
        /*if (mFirebaseAuth.getUid() != null) {
            RequestedDoneBooksViewModel requestedDoneBooksViewModel = ViewModelProviders.of(getActivity()).get(RequestedDoneBooksViewModel.class);
            requestedDoneBooksViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<RequestedDoneBooks>() {
                @Override
                public void onChanged(@android.support.annotation.Nullable RequestedDoneBooks requestedDoneBooks) {

                }
            });
            /*mFirebaseFirestore.collection("borrowedBooks").document(mFirebaseAuth.getUid())
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
                                                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                                                        mFirebaseFirestore.collection("users").document(book.getUid()).addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                                                                    User user = documentSnapshot.toObject(User.class);
                                                                                    String chatID = (String) dataSnapshot.getValue();
                                                                                    String userId = book.getUid();
                                                                                    chatIDs.add(chatID);
                                                                                    listBooks.add(book);
                                                                                    usersID.add(userId);
                                                                                    mUsersOwner.add(user);
                                                                                    mAdapter.setItems(listBooks, booksID, chatIDs, usersID, mUsersOwner);
                                                                                    mAdapter.notifyDataSetChanged();
                                                                                }
                                                                            }
                                                                        });
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
        }*/
        return view;
    }
}
