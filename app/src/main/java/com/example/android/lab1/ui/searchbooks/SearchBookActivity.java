package com.example.android.lab1.ui.searchbooks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.algolia.search.saas.AbstractQuery;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerSearchAdapter;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SearchBookActivity extends AppCompatActivity implements FilterDataPass {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_BOOKS_INDEX_NAME = "books";
    private final static String ALGOLIA_USERS_INDEX_NAME = "users";

    private final static int INTRO_VIEW = 0;
    private final static int OFFLINE_VIEW = 1;
    private final static int NO_RESULTS_VIEW = 2;
    private final static int RESULTS_VIEW = 3;

    private final static int POSITION_FINE_PERMISSION = 10;

    private boolean[] currentView = {true, false, false, false};

    boolean[] searchByFilters = {true, true, true, true};
    int[] seekBarsFilters = {0, FiltersValues.MAX_POSITION};
    boolean[] orderFilters = {true, false, false, false};

    private double currentLat;
    private double currentLong;

    private FloatingSearchView mSearchView;
    private TextView mIntroTextViewTop;
    private TextView mIntroTextViewBottom;
    private TextView mNoResultsTextViewTop;
    private TextView mNoResultsTextViewBottom;
    private TextView mNoConnectionTextViewTop;
    private TextView mNoConnectionTextViewBottom;
    private AppCompatButton mNoConnectionButton;
    private Index booksIndex;
    private Index usersIndex;
    private RecyclerView mRecyclerView;
    private RecyclerSearchAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationClient;

    private Query query = new Query();
    private String mCurrentUserId;

    private JSONArray hits;
    private ArrayList<BookSearchItem> backupDataSet;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);

        Utilities.setupStatusBarColor(this);

        mSearchView = findViewById(R.id.floating_search_view);
        mIntroTextViewTop = findViewById(R.id.search_book_intro_top);
        mIntroTextViewBottom = findViewById(R.id.search_book_intro_bottom);
        mNoResultsTextViewTop = findViewById(R.id.search_book_no_results_top);
        mNoResultsTextViewBottom = findViewById(R.id.search_book_no_results_bottom);
        mNoConnectionTextViewTop = findViewById(R.id.search_book_no_connection_top);
        mNoConnectionTextViewBottom = findViewById(R.id.search_book_no_connection_bottom);
        mNoConnectionButton = findViewById(R.id.search_book_no_connection_try_again);
        mRecyclerView = findViewById(R.id.results_recycler_view);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null)
            mCurrentUserId = currentUser.getUid();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Utilities.checkPermissionActivity(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Utilities.askPermissionActivity(this, Manifest.permission.ACCESS_FINE_LOCATION, POSITION_FINE_PERMISSION);
        } else {
            setCurrentLocation();
        }

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        booksIndex = client.getIndex(ALGOLIA_BOOKS_INDEX_NAME);
        usersIndex = client.getIndex(ALGOLIA_USERS_INDEX_NAME);

        query.setAroundRadius((seekBarsFilters[FiltersValues.POSITION_FILTER] + 10) * 100);

        setupMenuItemClickListener();
        setupSearchListener();
        setupQueryChangeListener();
        setupLeftItemListener();
        setupNoConnectionButtonClickListener();
        mRecyclerView.setAdapter(null);

        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("CURRENT_VIEW") != 0) {
                int currentViewIndex = savedInstanceState.getInt("CURRENT_VIEW");
                currentViewIndex--;
                Arrays.fill(currentView, false);
                currentView[currentViewIndex] = true;

                currentLat = savedInstanceState.getDouble("CURRENT_LAT");
                currentLong = savedInstanceState.getDouble("CURRENT_LONG");

                if (currentView[OFFLINE_VIEW])
                    showOfflineView();
                else if (currentView[NO_RESULTS_VIEW]) {
                    mNoResultsTextViewTop.setText(String.format(getResources().getString(R.string.no_results),
                            savedInstanceState.getString("NO_RESULT_SEARCH")));
                    showNoResultsView();
                    updateNoResultsTextView();
                } else if (currentView[RESULTS_VIEW]) {
                    backupDataSet = (ArrayList<BookSearchItem>) savedInstanceState.getSerializable("DATA_SET");
                    mAdapter = new RecyclerSearchAdapter(backupDataSet, currentLat, currentLong);
                    mRecyclerView.setAdapter(mAdapter);
                    showResultsView();
                }
            }
        } else {
            mSearchView.setSearchFocused(true);
        }

    }

    @SuppressLint("MissingPermission")
    private void setCurrentLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLat = location.getLatitude();
                            currentLong = location.getLongitude();
                            query.setAroundLatLng(new AbstractQuery.LatLng(currentLat, currentLong));
                        }
                    }
                });
    }

    private void setupLeftItemListener() {

        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                SearchBookActivity.this.finish();
            }
        });
    }

    private void setupNoConnectionButtonClickListener() {

        mNoConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preSearch(mSearchView.getQuery());
            }
        });
    }

    private void setupMenuItemClickListener() {
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                int itemID = item.getItemId();
                if (itemID == R.id.action_filter) {
                    showDialog();
                }
            }
        });
    }

    private void setupSearchListener() {
        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                preSearch(currentQuery);
            }
        });
    }

    private void setupQueryChangeListener() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                preSearch(newQuery);
            }
        });
    }

    private void preSearch(String searchString) {
        if (Utilities.isOnline(SearchBookActivity.this)) {
            if (searchString.isEmpty() || searchString.length() < 2) {
                if (!currentView[INTRO_VIEW])
                    showIntroView();
                return;
            }
            search(searchString);
        } else {
            if (!currentView[OFFLINE_VIEW])
                showOfflineView();
        }
    }

    private void search(final String searchString) {

        query.setQuery(searchString);
        mSearchView.showProgress();

        booksIndex.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(final JSONObject result, AlgoliaException error) {
                hits = result.optJSONArray("hits");
                if (hits.length() == 0) {
                    if (!currentView[NO_RESULTS_VIEW])
                        showNoResultsView();

                    updateNoResultsTextView();

                    mSearchView.hideProgress();
                    return;
                }

                final ArrayList<BookSearchItem> booksDataSet = new ArrayList<>();
                Set<String> bookUIDs = new HashSet<>();

                for (int i = 0; i < hits.length(); i++) {
                    JSONObject jsonBook = hits.optJSONObject(i);
                    String uid = jsonBook.optString("uid");
                    if (!uid.equals(mCurrentUserId)) {

                        bookUIDs.add(uid);
                        BookSearchItem book = new BookSearchItem();
                        book.setTitle(jsonBook.optString("title"));
                        book.setAuthors(jsonBook.optString("author"));
                        book.setCondition(jsonBook.optInt("conditions"));
                        book.setWebThumbnail(jsonBook.optString("thumbnail"));
                        book.setUid(uid);
                        book.setBookID(jsonBook.optString("objectID"));
                        JSONObject geoLocation = jsonBook.optJSONObject("_geoloc");
                        book.setGeoPoint(new GeoPoint(geoLocation.optDouble("lat"), geoLocation.optDouble("lng")));
                        booksDataSet.add(book);
                    }
                }

                usersIndex.getObjectsAsync(bookUIDs, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {

                        JSONArray usersResult = jsonObject.optJSONArray("results");
                        HashMap<String, UserInfo> bookUsers = new HashMap<>();
                        for (int i = 0; i < usersResult.length(); i++) {
                            JSONObject jsonUser = usersResult.optJSONObject(i);
                            UserInfo userInfo = new UserInfo();
                            userInfo.setImage(jsonUser.optString("image"));
                            userInfo.setRating(jsonUser.optDouble("rating"));
                            bookUsers.put(jsonUser.optString("objectID"), userInfo);
                        }

                        for (int i = 0; i < booksDataSet.size(); i++) {

                            UserInfo bookUser = bookUsers.get(booksDataSet.get(i).getUid());
                            if (bookUser.getRating() < (float) seekBarsFilters[FiltersValues.RATING_FILTER] / 10) {
                                booksDataSet.remove(i);
                                i--;
                            } else {
                                booksDataSet.get(i).setUserImage(bookUser.getImage());
                                booksDataSet.get(i).setUserRating(bookUser.getRating());
                            }
                        }

                        if(booksDataSet.size() > 0)
                        {
                            backupDataSet = new ArrayList<>(booksDataSet);
                            mAdapter = new RecyclerSearchAdapter(booksDataSet, currentLat, currentLong);
                            mRecyclerView.swapAdapter(mAdapter, false);

                            if (!currentView[RESULTS_VIEW])
                                showResultsView();
                        }
                        else
                        {
                            if(!currentView[NO_RESULTS_VIEW])
                                showNoResultsView();
                            updateNoResultsTextView();
                        }
                        mSearchView.hideProgress();




                    }
                });


            }
        });

    }

    private void showDialog() {
        Bundle filterData = new Bundle();
        filterData.putBooleanArray(FiltersValues.SEARCH_BY, searchByFilters);
        filterData.putIntArray(FiltersValues.SEEK_BARS, seekBarsFilters);
        filterData.putBooleanArray(FiltersValues.ORDER_BY, orderFilters);

        FragmentManager fm = getSupportFragmentManager();
        FiltersFragment filtersFragment = FiltersFragment.newInstance(filterData);
        filtersFragment.show(fm, "filters_fragment");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mSearchView.getQuery().length() > 2) {
            int currentViewIndex;
            if (currentView[INTRO_VIEW])
                currentViewIndex = INTRO_VIEW;
            else if (currentView[OFFLINE_VIEW])
                currentViewIndex = OFFLINE_VIEW;
            else if (currentView[NO_RESULTS_VIEW]) {
                currentViewIndex = NO_RESULTS_VIEW;
                outState.putString("NO_RESULT_SEARCH", mSearchView.getQuery());
            } else
                currentViewIndex = RESULTS_VIEW;

            outState.putInt("CURRENT_VIEW", ++currentViewIndex);
            outState.putSerializable("DATA_SET", backupDataSet);
            outState.putDouble("CURRENT_LAT", currentLat);
            outState.putDouble("CURRENT_LONG", currentLong);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void filterDataPass(Bundle bundle) {
        searchByFilters = bundle.getBooleanArray(FiltersValues.SEARCH_BY);
        seekBarsFilters = bundle.getIntArray(FiltersValues.SEEK_BARS);
        orderFilters = bundle.getBooleanArray(FiltersValues.ORDER_BY);


        List<String> searchFields = new ArrayList<>();
        if (searchByFilters[FiltersValues.SEARCH_BY_TITLE]) {
            searchFields.add("title");
        }
        if (searchByFilters[FiltersValues.SEARCH_BY_AUTHOR]) {
            searchFields.add("author");
        }
        if (searchByFilters[FiltersValues.SEARCH_BY_PUBLISHER]) {
            searchFields.add("publisher");
        }
//                        if (searchByFilters[FiltersValues.SEARCH_BY_TAGS]) {
//                                        searchFields.add("tags");
//                        }

        query.setRestrictSearchableAttributes(searchFields.toArray(new String[searchFields.size()]));
        query.setAroundRadius((seekBarsFilters[FiltersValues.POSITION_FILTER] + 10) * 100);

        preSearch(mSearchView.getQuery());
    }

    private void showOfflineView() {
        currentView[OFFLINE_VIEW] = true;
        currentView[NO_RESULTS_VIEW] = false;
        currentView[INTRO_VIEW] = false;
        currentView[RESULTS_VIEW] = false;

        mRecyclerView.setVisibility(View.GONE);
        if (mRecyclerView.getAdapter() == null)
            mRecyclerView.swapAdapter(null, true);
        mIntroTextViewTop.setVisibility(View.GONE);
        mIntroTextViewBottom.setVisibility(View.GONE);
        mNoResultsTextViewTop.setVisibility(View.GONE);
        mNoResultsTextViewBottom.setVisibility(View.GONE);
        mNoConnectionTextViewTop.setVisibility(View.VISIBLE);
        mNoConnectionTextViewBottom.setVisibility(View.VISIBLE);
        mNoConnectionButton.setVisibility(View.VISIBLE);
    }

    private void showResultsView() {
        currentView[OFFLINE_VIEW] = false;
        currentView[NO_RESULTS_VIEW] = false;
        currentView[INTRO_VIEW] = false;
        currentView[RESULTS_VIEW] = true;

        mNoConnectionTextViewTop.setVisibility(View.GONE);
        mNoConnectionTextViewBottom.setVisibility(View.GONE);
        mNoConnectionButton.setVisibility(View.GONE);
        mIntroTextViewTop.setVisibility(View.GONE);
        mIntroTextViewBottom.setVisibility(View.GONE);
        mNoResultsTextViewTop.setVisibility(View.GONE);
        mNoResultsTextViewBottom.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoResultsView() {
        currentView[OFFLINE_VIEW] = false;
        currentView[NO_RESULTS_VIEW] = true;
        currentView[INTRO_VIEW] = false;
        currentView[RESULTS_VIEW] = false;

        mRecyclerView.setVisibility(View.GONE);
        if (mRecyclerView.getAdapter() == null)
            mRecyclerView.swapAdapter(null, true);

        mNoConnectionTextViewTop.setVisibility(View.GONE);
        mNoConnectionTextViewBottom.setVisibility(View.GONE);
        mNoConnectionButton.setVisibility(View.GONE);
        mIntroTextViewTop.setVisibility(View.GONE);
        mIntroTextViewBottom.setVisibility(View.GONE);
        mNoResultsTextViewTop.setVisibility(View.VISIBLE);
        mNoResultsTextViewBottom.setVisibility(View.VISIBLE);
    }

    private void updateNoResultsTextView()
    {
        mNoResultsTextViewTop.setText(String.format(getResources().getString(R.string.no_results), mSearchView.getQuery()));
    }

    private void showIntroView() {
        currentView[OFFLINE_VIEW] = false;
        currentView[NO_RESULTS_VIEW] = false;
        currentView[INTRO_VIEW] = true;
        currentView[RESULTS_VIEW] = false;

        mRecyclerView.setVisibility(View.GONE);
        if (mRecyclerView.getAdapter() != null)
            mRecyclerView.swapAdapter(null, true);
        mNoConnectionTextViewTop.setVisibility(View.GONE);
        mNoConnectionTextViewBottom.setVisibility(View.GONE);
        mNoConnectionButton.setVisibility(View.GONE);
        mNoResultsTextViewTop.setVisibility(View.GONE);
        mNoResultsTextViewBottom.setVisibility(View.GONE);
        mIntroTextViewTop.setVisibility(View.VISIBLE);
        mIntroTextViewBottom.setVisibility(View.VISIBLE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case POSITION_FINE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setCurrentLocation();
                }
        }
    }

}
