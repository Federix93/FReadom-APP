package com.example.android.lab1.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerFavoriteBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.FavoriteBooksViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class FavoriteBooksActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    RecyclerFavoriteBookAdapter mAdapter;
    Toolbar mToolbar;
    LinearLayout mTextAdviceLayout;

    List<Book> mBooks;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseFirestore;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mRecyclerView = findViewById(R.id.rv_favorite);
        mTextAdviceLayout = findViewById(R.id.favorite_advice);

        mToolbar = findViewById(R.id.toolbar_favorite);

        mToolbar.setTitle(R.string.favorite_title);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Utilities.setupStatusBarColor(this);

        mBooks = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(null);

        mAdapter = new RecyclerFavoriteBookAdapter(mBooks);
        mRecyclerView.setAdapter(mAdapter);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getUid() != null) {
            FavoriteBooksViewModel favoriteBooksViewModel = ViewModelProviders.of(this).get(FavoriteBooksViewModel.class);
            favoriteBooksViewModel.getSnapshotLiveData().observe(this, new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> bookList) {
                    mAdapter.setItems(bookList);
                    mAdapter.notifyDataSetChanged();
                    if (bookList.size() > 0)
                        mTextAdviceLayout.setVisibility(GONE);
                }
            });
        } else {
            mTextAdviceLayout.setVisibility(View.VISIBLE);
        }
    }
}
