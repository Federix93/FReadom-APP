package com.example.android.lab1.ui.homepage;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MoreActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerBookAdapter mAdapter;
    private GeoPoint mCurrentPosition;
    private Toolbar mToolbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        mToolbar = findViewById(R.id.toolbar_more_activity);
        mRecyclerView = findViewById(R.id.rv_more);

        Utilities.setupStatusBarColor(this);
        mToolbar.setTitle("Nelle tue vicinanze");

        mCurrentPosition = new GeoPoint(getIntent().getDoubleExtra("latitude", 0),
                getIntent().getDoubleExtra("longitude", 0));

        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        queryDatabase(mCurrentPosition, null);

    }

    public void queryDatabase(GeoPoint currentPosition, String nodeID) {
        GeoPoint[] geoPoint = Utilities.buildBoundingBox(currentPosition.getLatitude(),
                currentPosition.getLongitude(),
                (double) 15000);
        Query query = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", geoPoint[0])
                .whereLessThan("geoPoint", geoPoint[1]).limit(30);
        query.get().addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Book> books = new ArrayList<>();
                if (queryDocumentSnapshots != null) {
                    for (Book b : queryDocumentSnapshots.toObjects(Book.class)) {
                        if (!b.getUid().equals(FirebaseAuth.getInstance().getUid()) /*&& b.getLoanStart() != -1*/) {
                            books.add(b);
                        }
                    }
                    Log.d("GNIPPO", "onSuccess: "+books);
                    mAdapter = new RecyclerBookAdapter(books);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.smoothScrollToPosition(0);

                }
            }
        });
    }
}
