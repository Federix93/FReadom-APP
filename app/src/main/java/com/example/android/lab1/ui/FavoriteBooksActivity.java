package com.example.android.lab1.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerFavoriteBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.FavoriteBooks;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class FavoriteBooksActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    RecyclerFavoriteBookAdapter mAdapter;
    Toolbar mToolbar;
    LinearLayout mTextAdviceLayout;

    List<Book> mBooks;
    List<String> mBookIds;

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

        mBookIds = new ArrayList<>();
        mBooks = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(null);

        mAdapter = new RecyclerFavoriteBookAdapter(mBooks, mBookIds, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        final DocumentReference documentReference;
        if (mFirebaseAuth.getUid() != null) {
            documentReference = mFirebaseFirestore.collection("favorites").document(mFirebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        FavoriteBooks favoriteBooks = documentSnapshot.toObject(FavoriteBooks.class);
                        if (favoriteBooks != null && !favoriteBooks.getBookIds().isEmpty()) {
                            for (final String s : favoriteBooks.getBookIds()) {
                                mFirebaseFirestore.collection("books").document(s).addSnapshotListener(FavoriteBooksActivity.this, new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                        if (documentSnapshot != null && documentSnapshot.exists()) {
                                            Book book = documentSnapshot.toObject(Book.class);
                                            mBooks.add(book);
                                            mBookIds.add(s);
                                            mAdapter.setItems(mBooks, mBookIds, getApplicationContext());
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }

                        } else {
                            mTextAdviceLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Devi essere loggato!", Toast.LENGTH_LONG).show();
        }
    }
}
