package com.example.android.lab1;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lab1.model.Book;
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
    private FloatingActionButton mFAB;

    Query mQuery;
    FirebaseFirestore mFirebaseFirestore;

    public HomeFragment()
    {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        mToolbar = getActivity().findViewById(R.id.toolbar_main_activity);
        mFAB = mRootView.findViewById(R.id.fab_add_book);
        mRecyclerView = mRootView.findViewById(R.id.recycler_books);

        if (container.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            NUM_COLUMNS = 2;
        } else {
            NUM_COLUMNS = 4;
        }


        mToolbar.setTitle(getString(R.string.app_name));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_home);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                switch (clickedId)
                {
                    case R.id.action_search:
                        Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.action_filter:
                        Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
                        break;

                }
                return true;
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),NUM_COLUMNS);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mQuery = mFirebaseFirestore.collection("books");
        mQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                List<Book> books = queryDocumentSnapshots.toObjects(Book.class);
                mAdapter = new RecyclerBookAdapter(books);
                mRecyclerView.setAdapter(mAdapter);
            }
        });

       mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mFAB.getVisibility() == View.VISIBLE) {
                    mFAB.hide();
                } else if (dy < 0 && mFAB.getVisibility() != View.VISIBLE) {
                    mFAB.show();
                }
            }
        });

        mFAB.setVisibility(View.VISIBLE);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoadBookActivity.class);
                startActivityForResult(intent, ADD_BOOK_REQUEST);
            }
        });

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFAB.setVisibility(View.GONE);
    }

}
