package com.example.android.lab1.ui.homepage;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final int ADD_BOOK_REQUEST = 1;
    private int NUM_COLUMNS;


    View mRootView;
    Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private RecyclerBookAdapter mAdapter;

    Query mQuery;
    FirebaseFirestore mFirebaseFirestore;

    public HomeFragment() {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        mToolbar = getActivity().findViewById(R.id.toolbar_home_page_activity);

        mRecyclerView = mRootView.findViewById(R.id.recycler_books);
        mRecyclerView.setAdapter(new RecyclerBookAdapter(null));

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            NUM_COLUMNS = 2;
        } else {
            NUM_COLUMNS = 4;
        }


        mToolbar.setTitle(getString(R.string.toolbar_title_home));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_home);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                switch (clickedId) {
                    case R.id.action_search:
                        Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), NUM_COLUMNS);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mQuery = mFirebaseFirestore.collection("books");
        mQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                List<Book> books = queryDocumentSnapshots.toObjects(Book.class);
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    for (int i = 0; i < books.size(); i++) {
                        if (books.get(i).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            books.remove(i);
                        }
                    }
                }

                mAdapter = new RecyclerBookAdapter(books);
                mRecyclerView.setAdapter(mAdapter);
            }
        });

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
