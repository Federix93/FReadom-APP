package com.example.android.lab1.ui.homepage;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ReyclerMoreAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MoreActivity extends AppCompatActivity {

    private final static int HITS_PER_PAGE = 50;
    private final static int LOAD_MORE_THRESHOLD = 20;

    private GridLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private ReyclerMoreAdapter mAdapter;
    private GeoPoint[] mGeoBox;
    private DocumentSnapshot lastVisible = null;
    private boolean endReached = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        Toolbar mToolbar = findViewById(R.id.toolbar_more_activity);
        mRecyclerView = findViewById(R.id.rv_more);

        Utilities.setupStatusBarColor(this);
        mToolbar.setTitle(R.string.nearby_home_page);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GeoPoint currentPosition = new GeoPoint(getIntent().getDoubleExtra("latitude", 0),
                getIntent().getDoubleExtra("longitude", 0));

        mGeoBox = Utilities.buildBoundingBox(currentPosition.getLatitude(),
                currentPosition.getLongitude(),
                (double) 15000);

        mLayoutManager = new GridLayoutManager(this, 3);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setRecyclerViewScrollListener();

        queryDatabase();


    }

    private void setRecyclerViewScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int currentItemCount = mLayoutManager.getItemCount();
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (currentItemCount == 0 || endReached) {
                    return;
                }

                if (currentItemCount - lastVisibleItem <= LOAD_MORE_THRESHOLD) {
                    queryDatabase();
                }
            }
        });
    }

    public void queryDatabase() {

        final Query query;
        if (lastVisible == null)
            query = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", mGeoBox[0])
                    .whereLessThan("geoPoint", mGeoBox[1]).orderBy("geoPoint").limit(HITS_PER_PAGE);
        else
            query = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", mGeoBox[0])
                    .whereLessThan("geoPoint", mGeoBox[1]).orderBy("geoPoint").startAfter(lastVisible).limit(HITS_PER_PAGE);

        query.get().addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Book> books = new ArrayList<>();
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                    if (queryDocumentSnapshots.size() < HITS_PER_PAGE)
                        endReached = true;
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (Book b : queryDocumentSnapshots.toObjects(Book.class)) {
                        if (!b.getUid().equals(FirebaseAuth.getInstance().getUid()) /*&& b.getLoanStart() != -1*/) {
                            books.add(b);
                        }
                    }
                    if (mAdapter == null) {
                        mAdapter = new ReyclerMoreAdapter(books);
                        mRecyclerView.setAdapter(mAdapter);
                    } else
                        mAdapter.addAll(books);
                } else {
                    endReached = true;
                }
            }
        });
    }


}
