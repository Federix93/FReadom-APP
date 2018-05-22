package com.example.android.lab1.ui.homepage;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import com.algolia.search.saas.AbstractQuery;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_BOOKS_INDEX_NAME = "books";
    private final static String ALGOLIA_USERS_INDEX_NAME = "users";

    private static final String LAT = "LAT";
    private static final String LON = "LAT";
    private static final String SELECTED_GENRE = "SG";
    private static final String CITY = "CITY";
    private static final String GENRES = "GENRES";
    private static final String GENRE_PLACE_HOLDER = "GENRE_PLACE_HOLDER";

    View mRootView;
    SwipeRefreshLayout mRefreshLayout;
    Toolbar mToolbar;
    AppCompatButton mGenreFilterButton;
    AppCompatButton mPositionFilterButton;
    TextView mFirstOtherTextView;
    TextView mSecondOtherTextView;
    ImageView mSearchImageView;
    FirebaseFirestore mFirebaseFirestore;

    private Index books;
    private Index users;
    private Bundle savedState = null;

    private RecyclerView mFirstRecyclerView;
    private RecyclerView mSecondRecyclerView;
    private boolean[] mRecyclerUpdating;
    private Integer mSelectedGenre;
    private GeoPoint mCurrentPosition;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        mToolbar = getActivity().findViewById(R.id.toolbar_home_page_activity);
        mRefreshLayout = mRootView.findViewById(R.id.homepage_refresh_layout);
        mFirstRecyclerView = mRootView.findViewById(R.id.first_recycler_books);
        mSecondRecyclerView = mRootView.findViewById(R.id.second_recycler_books);
        mGenreFilterButton = mRootView.findViewById(R.id.genre_filter_button);
        mPositionFilterButton = mRootView.findViewById(R.id.position_filter_button);
        mFirstOtherTextView = mRootView.findViewById(R.id.button_first_recycler_view);
        mSecondOtherTextView = mRootView.findViewById(R.id.button_second_recycler_view);
        mRecyclerUpdating = new boolean[2];
        for (int i = 0; i < mRecyclerUpdating.length; i++) {
            mRecyclerUpdating[i] = false;
        }

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        books = client.getIndex(ALGOLIA_BOOKS_INDEX_NAME);
        users = client.getIndex(ALGOLIA_USERS_INDEX_NAME);

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

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        queryDatabase();
                    }
                });
            }
        });

        getUserPosition();
        queryDatabase();

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

    private void getUserPosition() {
        /* TODO query firebase to see if user has ever setup position otherwise do
           TODO a location request asking permission first
         */
    }

    private void queryDatabase() {
        final com.algolia.search.saas.Query algoliaQuery1 = new com.algolia.search.saas.Query();
        com.algolia.search.saas.Query algoliaQuery2 = new com.algolia.search.saas.Query();
        final int homePageBooksNumber = getResources().getInteger(R.integer.homepage_book_number);

        String userId = null;
        if (FirebaseAuth.getInstance() != null && FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getUid() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        final String finalUserId = userId;
        if ((mCurrentPosition != null || mSelectedGenre != null)
                && getResources() != null) {
            if (mCurrentPosition != null) {
                int radius = getResources().getInteger(R.integer.position_radius_address);
                AbstractQuery.LatLng latLng = new AbstractQuery.LatLng(mCurrentPosition.getLatitude(),
                        mCurrentPosition.getLongitude());

                algoliaQuery1.setAroundLatLng(latLng);
                algoliaQuery1.setAroundRadius(radius);

                algoliaQuery2.setAroundLatLng(latLng);
                algoliaQuery2.setAroundRadius(radius);
            }

            String filters[] = new String[3];
            filters[0] = mSelectedGenre != null ? "genre=" + Integer.toString(mSelectedGenre) : "";
            filters[1] = finalUserId != null ? "NOT uid:\"" + finalUserId + "\"" : "";
            //filters[2] = "loanStart=0";
            if (!filters[0].equals("") && !filters[1].equals(""))
                algoliaQuery1.setFilters(TextUtils.join(" AND ", filters));
            else if (!filters[0].equals(""))
                algoliaQuery1.setFilters(filters[0]);
            else
                algoliaQuery1.setFilters(filters[1]);

            //algoliaQuery2.setFilters(TextUtils.join(" AND ", filters));

            algoliaQuery1.setHitsPerPage(homePageBooksNumber);
            algoliaQuery2.setHitsPerPage(homePageBooksNumber);


            books.searchAsync(algoliaQuery1, new CompletionHandler() {
                @Override
                public void requestCompleted(JSONObject result, AlgoliaException error) {
                    if (result != null && error == null) {
                        processFilteredResultIntoRecyclerView(result, mFirstRecyclerView);
                    }
                }
            });

            try {
                books.setSettingsAsync(new JSONObject().put("customRanking", new JSONArray()
                        .put("desc(timestamp)")), new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
                        if (jsonObject != null && e == null) {
                            books.searchAsync(algoliaQuery1, new CompletionHandler() {
                                @Override
                                public void requestCompleted(JSONObject result, AlgoliaException error) {
                                    if (result != null && error == null) {
                                        processFilteredResultIntoRecyclerView(result, mSecondRecyclerView);
                                    }
                                }
                            });
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        if (mCurrentPosition == null && mSelectedGenre == null) {
            // if no filters firebase can easily handle the queries
            CollectionReference books = mFirebaseFirestore.collection("books");
            Query query = books.whereEqualTo("loanStart", null);

            query.limit(20).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    mRecyclerUpdating[0] = true;
                    List<String> ids = new ArrayList<>();
                    List<Book> books = new ArrayList<>();
                    int size = 0;
                    Book currentBook;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        currentBook = documentSnapshot.toObject(Book.class);
                        if ((finalUserId != null && currentBook.getUid().equals(finalUserId))) {
                            continue;
                        }
                        ids.add(documentSnapshot.getId());
                        books.add(currentBook);
                        size++;
                        if (size > homePageBooksNumber)
                            break;
                    }

                    if (mFirstRecyclerView != null)
                        mFirstRecyclerView.setAdapter(new RecyclerBookAdapter(books, ids));

                    mRecyclerUpdating[0] = false;
                    if (!mRecyclerUpdating[1])
                        mRefreshLayout.setRefreshing(false);
                }
            });


            books.orderBy("timeInserted", Query.Direction.DESCENDING).limit(20).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    mRecyclerUpdating[1] = true;
                    List<String> ids = new ArrayList<>();
                    List<Book> books = new ArrayList<>();
                    int size = 0;
                    Book currentBook;
                    // apply filter
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        currentBook = documentSnapshot.toObject(Book.class);
                        if ((finalUserId != null && currentBook.getUid().equals(finalUserId)))
                            continue;
                        if(currentBook.getLoanStart() != null)
                            continue;
                        ids.add(documentSnapshot.getId());
                        books.add(currentBook);
                        size++;
                        if (size > homePageBooksNumber)
                            break;
                    }

                    if (mSecondRecyclerView != null)
                        mSecondRecyclerView.setAdapter(new RecyclerBookAdapter(books.subList(0, Math.min(books.size(), homePageBooksNumber)), ids));

                    mRecyclerUpdating[1] = false;
                    if (!mRecyclerUpdating[0])
                        mRefreshLayout.setRefreshing(false);
                }
            });
        }

    }

    private void processFilteredResultIntoRecyclerView(JSONObject result, RecyclerView recyclerView) {
        try {
            JSONArray hits = result.getJSONArray("hits");
            if (hits.length() > 0) {
                final ArrayList<Book> booksDataSet = new ArrayList<>();
                ArrayList<String> IDs = new ArrayList<>();

                for (int i = 0; i < hits.length(); i++) {
                    JSONObject jsonBook = hits.optJSONObject(i);
                    String uid = jsonBook.optString("uid");

                    Book book = new Book();
                    book.setTitle(jsonBook.optString("title"));
                    book.setAuthors(jsonBook.optString("author"));
                    book.setCondition(jsonBook.optInt("conditions"));
                    book.setWebThumbnail(jsonBook.optString("thumbnail"));
                    book.setUid(uid);
                    IDs.add(jsonBook.optString("objectID"));
                    JSONObject geoLocation = jsonBook.optJSONObject("_geoloc");
                    book.setGeoPoint(new GeoPoint(geoLocation.optDouble("lat"), geoLocation.optDouble("lng")));
                    booksDataSet.add(book);
                }

                recyclerView.setAdapter(new RecyclerBookAdapter(booksDataSet, IDs));
            }
        } catch (JSONException ex) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.POSITION_ACTIVITY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Address address = Utilities.readResultOfPositionActivity(data);
                        if (address != null) {
                            mCurrentPosition = new GeoPoint(address.getLat(), address.getLon());
                            mPositionFilterButton.setText(address.getAddress());
                            queryDatabase();
                        }
                    }
                }
                break;
            case Constants.PICK_GENRE:
                if (resultCode == Activity.RESULT_OK) {
                    Integer oldSelectedGenre = mSelectedGenre;
                    mSelectedGenre = data.hasExtra(GenreBooksActivity.SELECTED_GENRE) &&
                            GenreBooksActivity.isValidGenre(data.getIntExtra(GenreBooksActivity.SELECTED_GENRE, -1)) ?
                            data.getIntExtra(GenreBooksActivity.SELECTED_GENRE, -1) : null;
                    // since is an activity result we may have lost context so check it
                    if (getActivity() != null && getActivity().getApplicationContext() != null)
                        mGenreFilterButton.setText(mSelectedGenre != null ?
                                getActivity().getApplicationContext()
                                        .getResources()
                                        .getStringArray(R.array.genre)[mSelectedGenre] :
                                getActivity().getApplicationContext()
                                        .getResources()
                                        .getString(R.string.genre_filter_home_page));
                    queryDatabase();
                }
                break;
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedGenre != null) {
            outState.putInt(SELECTED_GENRE, mSelectedGenre);
        }
        if (mCurrentPosition != null) {
            outState.putString(CITY, mPositionFilterButton.getText().toString());
            outState.putDouble(LAT, mCurrentPosition.getLatitude());
            outState.putDouble(LON, mCurrentPosition.getLongitude());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_GENRE)) {
                mSelectedGenre = savedInstanceState.getInt(SELECTED_GENRE);
                mGenreFilterButton.setText(getResources().getStringArray(R.array.genre)[mSelectedGenre]);
            }
            if (savedInstanceState.containsKey(LAT) &&
                    savedInstanceState.containsKey(LON) &&
                    savedInstanceState.containsKey(CITY)) {
                mCurrentPosition = new GeoPoint(savedInstanceState.getDouble(LAT),
                        savedInstanceState.getDouble(LON));
                mPositionFilterButton.setText(savedInstanceState.getString(CITY));
            }
            queryDatabase();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.fragment_home, menu);
        mSearchImageView = (ImageView) menu.findItem(R.id.action_search).getActionView();
    }
}
