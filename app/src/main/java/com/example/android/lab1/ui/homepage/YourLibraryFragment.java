package com.example.android.lab1.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
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

    android.support.v7.widget.Toolbar mToolbar;
    FloatingActionButton mFab;
    RecyclerYourLibraryAdapter mAdapter;
    List<Book> mBooks;
    List<String> mBookIds;

    public void YourLibraryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_your_library, container, false);

        mRecyclerView = view.findViewById(R.id.rv_fragment_books_library);
        mFab = view.findViewById(R.id.fab_your_library);

        mToolbar = getActivity().findViewById(R.id.toolbar_home_page_activity);

        mToolbar.setTitle(R.string.your_library_fragment);
        mToolbar.getMenu().clear();

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        mBookIds = new ArrayList<>();
        mBooks = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(null);

        mAdapter = new RecyclerYourLibraryAdapter(mBooks, mBookIds, getActivity());

        firebaseFirestore.collection("books").whereEqualTo("uid", mFirebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        mBooks = queryDocumentSnapshots.toObjects(Book.class);
                        for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                            mBookIds.add(d.getId());
                        }
                        updateListOfBooks(mBooks, mBookIds);
                    }
                });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), LoadBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, ADD_BOOK_ACTIVITY);
            }
        });
        return view;
    }

    public void updateListOfBooks(List<Book> listBooksOfUser, List<String> bookIds) {
        mAdapter = new RecyclerYourLibraryAdapter(listBooksOfUser, bookIds, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mAdapter.notifyDataSetChanged();
//    }
}
