package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.LentBookAdapter;
import com.example.android.lab1.adapter.RecyclerDashboardLibraryAdapter;
import com.example.android.lab1.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LentFragmentItem extends Fragment {

    private RecyclerView mRecyclerView;
    List<Book> mBookIds;

    public LentFragmentItem() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);

        /*
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
                });*/
        return view;
    }
    public void updateListOfBooks(List<Book> mListBooksOfUser, List<String> bookIds) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        LentBookAdapter adapter = new LentBookAdapter(mListBooksOfUser, bookIds);
        mRecyclerView.setAdapter(adapter);
    }

}

