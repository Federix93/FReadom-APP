package com.example.android.lab1.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerHistoryAdapter;
import com.example.android.lab1.adapter.TagsAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    RecyclerHistoryAdapter mAdapter;
    Toolbar mToolbar;
/*
    TextView mHistoryVerb;
    ImageView mHistoryArrow;
    TextView mHistoryBookTitle;
    ImageView mHistoryBookThumbnail;
    TextView mHistoryToUser;
    TextView mHistoryUser;
    ImageView mHistoryUserImage;
    TextView mHistoryLeaveRating;
    TextView mHistoryDateFrom;
    TextView mHistoryDateTo;*/

    List<Book> mBooks;
    List<User> mUsers;

    FirebaseAuth mFirebaseAuth;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//        mHistoryVerb = findViewById(R.id.history_verb);
//        mHistoryArrow = findViewById(R.id.history_arrow);
//        mHistoryBookTitle = findViewById(R.id.history_title);
//        mHistoryBookThumbnail = findViewById(R.id.history_thumbnail);
//        mHistoryToUser = findViewById(R.id.history_to_user);
//        mHistoryUser = findViewById(R.id.history_username);
//        mHistoryUserImage = findViewById(R.id.history_user_image);
//        mHistoryLeaveRating = findViewById(R.id.history_leave_rating);
//        mHistoryDateFrom = findViewById(R.id.history_from_date);
//        mHistoryDateTo = findViewById(R.id.history_to_date);

        mToolbar = findViewById(R.id.toolbar_history);
        mToolbar.setTitle(R.string.history);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Utilities.setupStatusBarColor(this);

        mBooks = new ArrayList<>();
        mUsers = new ArrayList<>();

        mRecyclerView = findViewById(R.id.rv_history);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerHistoryAdapter(this, mBooks);
        mRecyclerView.setAdapter(mAdapter);

        mFirebaseAuth = FirebaseAuth.getInstance();

        Log.d("VINCI", "onCreate: Ci entro?");

        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Query query = firebaseFirestore.collection("history").whereEqualTo("uid", mFirebaseAuth.getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().isEmpty()){
                    Log.d("VINCI", "task null");
                    Query query1 = firebaseFirestore.collection("history").whereEqualTo("lentTo", mFirebaseAuth.getUid());
                    query1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot secondQueryDocumentSnapshots) {
                            if (secondQueryDocumentSnapshots != null) {
                                mBooks.addAll(secondQueryDocumentSnapshots.toObjects(Book.class));
                                mAdapter.setItems(mBooks);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    Log.d("VINCI", "onCreate: task not null");
                    mBooks.addAll(task.getResult().toObjects(Book.class));
                    mAdapter.setItems(mBooks);
                    mAdapter.notifyDataSetChanged();
                    Query query1 = firebaseFirestore.collection("history").whereEqualTo("lentTo", mFirebaseAuth.getUid());
                    query1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot secondQueryDocumentSnapshots) {
                            if (secondQueryDocumentSnapshots != null) {
                                mBooks.addAll(secondQueryDocumentSnapshots.toObjects(Book.class));
                                mAdapter.setItems(mBooks);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });

        query.get().addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d("VINCI", "ON CANCELLED");
            }
        });
        /*
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    mBooks.addAll(queryDocumentSnapshots.toObjects(Book.class));
                    mAdapter.setItems(mBooks);
                    mAdapter.notifyDataSetChanged();
                    Log.d("VINCI", "NOT EMPTY" + mBooks.size());

                    Query query1 = firebaseFirestore.collection("history").whereEqualTo("lentTo", mFirebaseAuth.getUid());
                    query1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot secondQueryDocumentSnapshots) {
                            if (secondQueryDocumentSnapshots != null) {
                                mBooks.addAll(secondQueryDocumentSnapshots.toObjects(Book.class));
                                mAdapter.setItems(mBooks);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });*/
    }
}
