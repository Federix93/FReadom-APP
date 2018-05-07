package com.example.android.lab1.ui.homepage;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.GenreBooksActivity;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.example.android.lab1.ui.searchbooks.SearchBookActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int ADD_BOOK_REQUEST = 1;

    View mRootView;
    Toolbar mToolbar;
    AppCompatButton mGenreFilterButton;
    AppCompatButton mPositionFilterButton;
    TextView mFirstOtherTextView;
    TextView mSecondOtherTextView;

    private RecyclerView mFirstRecyclerView;
    private RecyclerView mSecondRecyclerView;
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

        mFirstRecyclerView = mRootView.findViewById(R.id.first_recycler_books);
        mSecondRecyclerView = mRootView.findViewById(R.id.second_recycler_books);
        mGenreFilterButton = mRootView.findViewById(R.id.genre_filter_button);
        mPositionFilterButton = mRootView.findViewById(R.id.position_filter_button);
        mFirstOtherTextView = mRootView.findViewById(R.id.button_first_recycler_view);
        mSecondOtherTextView = mRootView.findViewById(R.id.button_second_recycler_view);

        mToolbar.setTitle(getString(R.string.toolbar_title_home));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_home);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                switch (clickedId) {
                    case R.id.action_search:
                        Intent intent = new Intent(getActivity(), SearchBookActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        mGenreFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), GenreBooksActivity.class);
//                startActivity(intent);
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mPositionFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mFirstOtherTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mSecondOtherTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });



        mFirstRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper firstSnapHelperStart = new GravitySnapHelper(Gravity.START);
        mFirstRecyclerView.setNestedScrollingEnabled(true);
        firstSnapHelperStart.attachToRecyclerView(mFirstRecyclerView);

        mSecondRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper secondSnapHelperStart = new GravitySnapHelper(Gravity.START);
        mSecondRecyclerView.setNestedScrollingEnabled(true);
        secondSnapHelperStart.attachToRecyclerView(mFirstRecyclerView);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mQuery = mFirebaseFirestore.collection("books");
        mQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                List<String> IDs = new ArrayList<>();
                for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                    IDs.add(d.getId());
                }
                List<Book> books = queryDocumentSnapshots.toObjects(Book.class);
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    for (int i = 0; i < books.size(); i++) {
                        if (books.get(i).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            books.remove(i);
                            IDs.remove(i);
                            i--;
                        }
                    }
                }
                mAdapter = new RecyclerBookAdapter(books, IDs);
                mFirstRecyclerView.setAdapter(mAdapter);
                mSecondRecyclerView.setAdapter(mAdapter);
            }
        });

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
