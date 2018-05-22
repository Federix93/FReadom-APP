package com.example.android.lab1.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerDashboardLibraryAdapter;
import com.example.android.lab1.adapter.RecyclerYourLibraryAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.LoadBookActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.lab1.ui.homepage.HomePageActivity.ADD_BOOK_ACTIVITY;

public class YourLibraryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    FloatingActionButton mFabLibrary;
    RecyclerYourLibraryAdapter mAdapter;
    List<Book> mBookIds;

    public void YourLibraryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_your_library, container, false);

        mRecyclerView = view.findViewById(R.id.rv_fragment_books_library);
        mFabLibrary = view.findViewById(R.id.fab_your_library);

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        firebaseFirestore.collection("books").whereEqualTo("uid", mFirebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        mBookIds = queryDocumentSnapshots.toObjects(Book.class);
                        List<String> IDs = new ArrayList<>();
                        for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                            IDs.add(d.getId());
                        }
                        updateListOfBooks(mBookIds, IDs);

                    }
                });

        mFabLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), LoadBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, ADD_BOOK_ACTIVITY);
            }
        });
        return view;
    }

    public void updateListOfBooks(List<Book> mListBooksOfUser, List<String> bookIds) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerYourLibraryAdapter(mListBooksOfUser, bookIds);
        mRecyclerView.setAdapter(mAdapter);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mAdapter.notifyDataSetChanged();
//    }
}
