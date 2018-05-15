package com.example.android.lab1.ui.homepage;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.model.Address;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.GenreBooksActivity;
import com.example.android.lab1.ui.searchbooks.SearchBookActivity;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.example.android.lab1.utils.Utilities;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HomeFragment extends Fragment {

    private static final int ADD_BOOK_REQUEST = 1;
    private static final String SHOWCASE_ID = "HomeFragment";

    View mRootView;
    Toolbar mToolbar;
    AppCompatButton mGenreFilterButton;
    AppCompatButton mPositionFilterButton;
    TextView mFirstOtherTextView;
    TextView mSecondOtherTextView;
    ImageView mSearchImageView;


    FirebaseFirestore mFirebaseFirestore;
    private RecyclerView mFirstRecyclerView;
    private RecyclerView mSecondRecyclerView;

    private Integer mSelectedGenre;
    private GeoPoint mCurrentPosition;

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
                                                      Intent i = new Intent(getActivity(), GenreBooksActivity.class);
                                                      i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                      getActivity().startActivityForResult(i, Constants.PICK_GENRE);
                                                  }
                                              }
        );

        mPositionFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent positionActivityIntent = Utilities.getPositionActivityIntent(getActivity(), true);
                getActivity().startActivityForResult(positionActivityIntent, Constants.POSITION_ACTIVITY_REQUEST);
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

        queryDatabase();

        /*books.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                List<String> IDs = new ArrayList<>();
                for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
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
                // order by time stamp
                List<Book> orderedByTime = new ArrayList<>(books);
                List<String> orderedByTimeIds = new ArrayList<>(IDs);
                Object temp[] = new Object[2];
                Long first, second;
                for (int i = 0; i < orderedByTime.size() - 1; i++) {
                    for (int i1 = 0; i1 < orderedByTime.size(); i1++) {
                        first = orderedByTime.get(i).getTimeInserted();
                        second = orderedByTime.get(i1).getTimeInserted();
                        if (first == null)
                            first = 1 / System.currentTimeMillis();
                        if (second == null)
                            second = 1/ System.currentTimeMillis();

                        if (first < second)
                        {
                            temp[0] = orderedByTime.get(i);
                            temp[1] = orderedByTimeIds.get(i);

                            orderedByTime.set(i, orderedByTime.get(i1));
                            orderedByTimeIds.set(i, orderedByTimeIds.get(i1));

                            orderedByTime.set(i1, (Book) temp[0]);
                            orderedByTimeIds.set(i1, (String) temp[1]);
                        }
                    }
                }

                mSecondRecyclerView.setAdapter(new RecyclerBookAdapter(orderedByTime, orderedByTimeIds));
            }
        });*/
        if (SharedPreferencesManager.getInstance(getActivity()).isFirstRun()) {
            Resources res = getResources();
            SharedPreferencesManager.getInstance(getActivity()).putFirstRun(false);
            TapTargetSequence tapTargetSequence = new TapTargetSequence(getActivity());
            tapTargetSequence.continueOnCancel(true);
            tapTargetSequence.targets(
                    TapTarget.forToolbarMenuItem(mToolbar, R.id.action_search,
                            res.getString(R.string.tutorial_title_search))
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(R.color.background_app)
                            .textColor(R.color.white),
                    TapTarget.forView(mGenreFilterButton,
                            res.getString(R.string.tutorial_title_genre))
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(R.color.background_app)
                            .transparentTarget(true)
                            .textColor(R.color.white),
                    TapTarget.forView(mPositionFilterButton,
                            res.getString(R.string.tutorial_title_position))
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(R.color.background_app)
                            .transparentTarget(true)
                            .textColor(R.color.white));
            tapTargetSequence.start();
        }

        return mRootView;
    }

    private void queryDatabase() {

        CollectionReference books = mFirebaseFirestore.collection("books");


        Query query1, query2;
        query1 = query2 = null;

        if (mCurrentPosition != null) {
            GeoPoint[] geoPoints = buildBoundingBox(mCurrentPosition.getLatitude(),
                    mCurrentPosition.getLongitude(),
                    (double) getResources().getInteger(R.integer.position_radius_address) / 1000);
            query1 = books.whereGreaterThan("geoPoint", geoPoints[0])
                    .whereLessThan("geoPoint", geoPoints[1]);
            query2 = books.whereGreaterThan("geoPoint", geoPoints[0])
                    .whereLessThan("geoPoint", geoPoints[1]);
        }

        String userId = null;
        if (FirebaseAuth.getInstance() != null
                && FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getUid() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        final int homePageBooksNumber = getResources().getInteger(R.integer.homepage_book_number);

        if (query1 == null)
            query1 = books;

        if (query2 == null) {
            query2 = books;
        }

        final String finalUserId = userId;
        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> ids = new ArrayList<>();
                List<Book> books = new ArrayList<>();
                int size = 0;
                Book currentBook;
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    currentBook = documentSnapshot.toObject(Book.class);
                    if ((finalUserId != null &&
                            currentBook.getUid().equals(finalUserId)) ||
                            (mSelectedGenre != null &&
                                    !mSelectedGenre.equals(currentBook.getGenre())))
                        continue;
                    ids.add(documentSnapshot.getId());
                    books.add(currentBook);
                    size++;
                    if (size > homePageBooksNumber)
                        break;
                }

                if (mFirstRecyclerView != null)
                    mFirstRecyclerView.setAdapter(new RecyclerBookAdapter(books, ids));
            }
        });


        query2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<String> ids = new ArrayList<>();
                List<Book> books = new ArrayList<>();
                int size = 0;
                Book currentBook;
                // apply filter
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    currentBook = documentSnapshot.toObject(Book.class);
                    if ((finalUserId != null &&
                            currentBook.getUid().equals(finalUserId)) ||
                            (mSelectedGenre != null &&
                                    !mSelectedGenre.equals(currentBook.getGenre())))
                        continue;
                    ids.add(documentSnapshot.getId());
                    books.add(currentBook);
                    size++;
                }

                Collections.sort(books);

                if (mSecondRecyclerView != null)
                    mSecondRecyclerView.setAdapter(new RecyclerBookAdapter(books.subList(0, Math.min(books.size(), homePageBooksNumber)), ids));
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.POSITION_ACTIVITY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Address address = Utilities.readResultOfPositionActivity(data);
                    mCurrentPosition = new GeoPoint(address.getLat(), address.getLon());
                    mPositionFilterButton.setText(address.getAddress());
                    queryDatabase();
                }
                break;
            case Constants.PICK_GENRE:
                if (resultCode == Activity.RESULT_OK) {
                    Integer oldSelectedGenre = mSelectedGenre;
                    mSelectedGenre = data.hasExtra(GenreBooksActivity.SELECTED_GENRE) &&
                            GenreBooksActivity.isValidGenre(data.getIntExtra(GenreBooksActivity.SELECTED_GENRE, -1)) ?
                            data.getIntExtra(GenreBooksActivity.SELECTED_GENRE, -1) : null;
                    mGenreFilterButton.setText(mSelectedGenre != null ?
                            getResources().getStringArray(R.array.genre)[mSelectedGenre] :
                            getString(R.string.genre_filter_home_page));
                    if (mSelectedGenre != oldSelectedGenre)
                        queryDatabase();
                }
                break;
        }

    }

    private GeoPoint[] buildBoundingBox(Double latitude, Double longitude, Double distanceInKm) {

        // ~1 mile of lat and lon in degrees
        Double lat = 0.0144927536231884;
        Double lon = 0.0181818181818182;

        Double lowerLat = latitude - (lat * distanceInKm);
        Double lowerLon = longitude - (lon * distanceInKm);

        Double greaterLat = latitude + (lat * distanceInKm);
        Double greaterLon = longitude + (lon * distanceInKm);

        GeoPoint lesserGeoPoint = new GeoPoint(lowerLat, lowerLon);
        GeoPoint greaterGeoPoint = new GeoPoint(greaterLat, greaterLon);

        return new GeoPoint[]{lesserGeoPoint, greaterGeoPoint};
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.fragment_home, menu);
        mSearchImageView = (ImageView) menu.findItem(R.id.action_search).getActionView();
    }
}
