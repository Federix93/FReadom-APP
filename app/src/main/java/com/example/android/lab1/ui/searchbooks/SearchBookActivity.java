package com.example.android.lab1.ui.searchbooks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
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
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.example.android.lab1.utils.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;

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

    private static final int HITS_PER_PAGE = 25;
    private static final int LOAD_MORE_THRESHOLD = 10;

    private int lastSearchedSeqNo;
    private int lastDisplayedSeqNo;
    private int lastRequestedPage;
    private int lastDisplayedPage;
    private boolean endReached;

    private Index booksIndex;
    private Index usersIndex;

    private final static int INTRO_VIEW = 0;
    private final static int OFFLINE_VIEW = 1;
    private final static int NO_RESULTS_VIEW = 2;
    private final static int RESULTS_VIEW = 3;
    private final static int UNKNOWN_ERROR_VIEW = 4;

    private boolean[] currentView = {true, false, false, false, false};

    boolean[] searchByFilters = {true, true, true, true};
    int[] seekBarsFilters = {0, FiltersValues.MAX_POSITION};

    private FloatingSearchView mSearchView;

    private TextView mIntroTextViewTop;
    private TextView mIntroTextViewBottom;
    private TextView mNoResultsTextViewTop;
    private TextView mNoResultsTextViewBottom;
    private TextView mNoConnectionTextViewTop;
    private TextView mNoConnectionTextViewBottom;
    private TextView mUnknownErrorTextViewTop;
    private TextView mUnknownErrorTextViewBottom;
    private AppCompatButton mNoConnectionButton;

    private int mShortAnimationDuration;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerSearchAdapter mAdapter;

    private Query booksQuery = new Query();
    private Query usersQuery = new Query();
    private String mCurrentUserId;

    private JSONArray hits;
    private ArrayList<BookSearchItem> backupDataSet;

    private SharedPreferences mSharedPref;
    private GeoPoint mCurrentPosition;

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
        mUnknownErrorTextViewTop = findViewById(R.id.search_book_unknown_error_top);
        mUnknownErrorTextViewBottom = findViewById(R.id.search_book_unknown_error_bottom);
        mRecyclerView = findViewById(R.id.results_recycler_view);

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null)
            mCurrentUserId = currentUser.getUid();

        mSharedPref = getPreferences(Context.MODE_PRIVATE);
        Gson filterGson = new Gson();
        String filterJson = mSharedPref.getString("FILTERS", "");


        if (!filterJson.isEmpty()) {
            FilterObject filterObject = filterGson.fromJson(filterJson, FilterObject.class);
            searchByFilters = filterObject.getSearchByFilters().clone();
            seekBarsFilters = filterObject.getSeekBarFilters().clone();
        }

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        booksIndex = client.getIndex(ALGOLIA_BOOKS_INDEX_NAME);
        usersIndex = client.getIndex(ALGOLIA_USERS_INDEX_NAME);
        booksQuery.setHitsPerPage(HITS_PER_PAGE);
        usersQuery.setHitsPerPage(HITS_PER_PAGE);
        usersQuery.setQuery("");

        booksIndex.enableSearchCache();
        usersIndex.enableSearchCache();

        setupMenuItemClickListener();
        setupSearchListener();
        setupQueryChangeListener();
        setupLeftItemListener();
        setupNoConnectionButtonClickListener();

        mCurrentPosition = SharedPreferencesManager.getInstance(this).getPosition();

        mAdapter = new RecyclerSearchAdapter(null, mCurrentPosition.getLatitude(), mCurrentPosition.getLongitude());
        mRecyclerView.setAdapter(mAdapter);

        booksQuery.setAroundLatLng(new AbstractQuery.LatLng(mCurrentPosition.getLatitude(), mCurrentPosition.getLongitude()));
        booksQuery.setFilters("lentTo:\"none\"");

        applyFilters();
        setRecyclerViewScrollListener();

        if (savedInstanceState == null) {
            mSearchView.setSearchFocused(true);
        }

    }

    private void setRecyclerViewScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int currentItemCount = mLinearLayoutManager.getItemCount();
                int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                if (currentItemCount == 0 || endReached) {
                    return;
                }

                if (lastRequestedPage > lastDisplayedPage) {
                    return;
                }

                if (currentItemCount - lastVisibleItem <= LOAD_MORE_THRESHOLD) {
                    loadMore();
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
        if (searchString.isEmpty() || searchString.length() < 2) {
            if (!currentView[INTRO_VIEW]) {
                showIntroView();
            }
            return;
        }
        search(searchString);
    }

    private void search(final String searchString) {

        final int currentSearchSeqNo = ++lastSearchedSeqNo;
        lastRequestedPage = 0;
        lastDisplayedPage = -1;
        endReached = false;

        booksQuery.setQuery(searchString);
        mSearchView.showProgress();
        booksIndex.searchAsync(booksQuery, new CompletionHandler() {
            @Override
            public void requestCompleted(final JSONObject result, AlgoliaException error) {
                if (result != null && error == null) {
                    if (currentSearchSeqNo <= lastDisplayedSeqNo) {
                        mSearchView.hideProgress();
                        return;
                    }
                    hits = result.optJSONArray("hits");
                    if (hits.length() == 0) {
                        endReached = true;

                        updateNoResultsTextView(booksQuery.getQuery());
                        if (!currentView[NO_RESULTS_VIEW])
                            showNoResultsView();


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

                    if (bookUIDs.size() == 0) {
                        updateNoResultsTextView(booksQuery.getQuery());
                        if (!currentView[NO_RESULTS_VIEW])
                            showNoResultsView();


                        mSearchView.hideProgress();
                        return;
                    }

                    StringBuilder stringBuilder = new StringBuilder(400);
                    for (String s : bookUIDs) {
                        stringBuilder.append("objectID:");
                        stringBuilder.append(s);
                        stringBuilder.append(" OR ");
                    }

                    stringBuilder.trimToSize();
                    stringBuilder.delete(stringBuilder.capacity() - 4, stringBuilder.capacity());
                    stringBuilder.trimToSize();

                    usersQuery.setFilters(stringBuilder.toString());

                    usersIndex.searchAsync(usersQuery, new CompletionHandler() {
                        @Override
                        public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
                            if (jsonObject != null && e == null) {

                                JSONArray usersResult = jsonObject.optJSONArray("hits");
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

                                mAdapter.clear();
                                if (booksDataSet.size() > 0) {
                                    backupDataSet = new ArrayList<>(booksDataSet);
                                    mAdapter.addAll(booksDataSet);
                                    lastDisplayedSeqNo = currentSearchSeqNo;
                                    lastDisplayedPage = 0;
                                    if (!currentView[RESULTS_VIEW])
                                        showResultsView();
                                } else {
                                    endReached = true;
                                    updateNoResultsTextView(booksQuery.getQuery());
                                    if (!currentView[NO_RESULTS_VIEW])
                                        showNoResultsView();

                                }
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.smoothScrollToPosition(0);
                                mSearchView.hideProgress();

                            } else if (e != null) {
                                manageSearchError();
                            }
                        }

                    });
                } else if (error != null) {
                    manageSearchError();
                }


            }
        });
    }

    private void loadMore() {
        Query loadMoreQuery = new Query(booksQuery);
        loadMoreQuery.setPage(++lastRequestedPage);
        booksQuery.setFilters("lentTo:\"none\"");
        final int currentSearchSeqNo = lastSearchedSeqNo;

        mSearchView.showProgress();

        booksIndex.searchAsync(loadMoreQuery, new CompletionHandler() {
            @Override
            public void requestCompleted(final JSONObject result, AlgoliaException error) {
                if (result != null && error == null) {

                    if (lastDisplayedSeqNo != currentSearchSeqNo) {
                        mSearchView.hideProgress();
                        return;
                    }

                    hits = result.optJSONArray("hits");
                    if (hits.length() == 0) {
                        endReached = true;
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

                    StringBuilder stringBuilder = new StringBuilder(400);
                    for (String s : bookUIDs) {
                        stringBuilder.append("objectID:");
                        stringBuilder.append(s);
                        stringBuilder.append(" OR ");
                    }

                    stringBuilder.trimToSize();
                    stringBuilder.delete(stringBuilder.capacity() - 4, stringBuilder.capacity());
                    stringBuilder.trimToSize();

                    usersQuery.setFilters(stringBuilder.toString());

                    usersIndex.searchAsync(usersQuery, new CompletionHandler() {
                        @Override
                        public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {

                            if (jsonObject != null && e == null) {
                                JSONArray usersResult = jsonObject.optJSONArray("hits");
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

                                if (booksDataSet.size() > 0) {
                                    backupDataSet.addAll(booksDataSet);
                                    mAdapter.addAll(booksDataSet);
                                    lastDisplayedPage = lastRequestedPage;
                                    if (!currentView[RESULTS_VIEW])
                                        showResultsView();
                                } else {
                                    endReached = true;
                                }
                                mAdapter.notifyDataSetChanged();
                                mSearchView.hideProgress();

                            } else if (e != null) {
                                manageSearchError();
                            }
                        }

                    });
                } else if (error != null) {
                    manageSearchError();
                }

            }
        });
    }

    private void showDialog() {
        Bundle filterData = new Bundle();
        filterData.putBooleanArray(FiltersValues.SEARCH_BY, searchByFilters);
        filterData.putIntArray(FiltersValues.SEEK_BARS, seekBarsFilters);

        FragmentManager fm = getSupportFragmentManager();
        FiltersFragment filtersFragment = FiltersFragment.newInstance(filterData);
        filtersFragment.show(fm, "filters_fragment");
    }

    @Override
    public void filterDataPass(Bundle bundle) {
        searchByFilters = bundle.getBooleanArray(FiltersValues.SEARCH_BY).clone();
        seekBarsFilters = bundle.getIntArray(FiltersValues.SEEK_BARS).clone();

        FilterObject filterObject = new FilterObject(searchByFilters, seekBarsFilters);

        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        Gson filtersGson = new Gson();
        String filtersJson = filtersGson.toJson(filterObject);
        prefsEditor.putString("FILTERS", filtersJson);
        prefsEditor.apply();

        applyFilters();

        preSearch(mSearchView.getQuery());
    }

    private void applyFilters() {
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
        if (searchByFilters[FiltersValues.SEARCH_BY_TAGS]) {
            searchFields.add("tags");
        }

//        searchFields.add("lentTo");

        booksQuery.setRestrictSearchableAttributes(searchFields.toArray(new String[searchFields.size()]));
        booksQuery.setAroundRadius((seekBarsFilters[FiltersValues.POSITION_FILTER] + 10) * 100);
    }

    private void manageSearchError() {
        mSearchView.hideProgress();

        if (Utilities.isOnline(SearchBookActivity.this)) {
            if (!currentView[UNKNOWN_ERROR_VIEW])
                showUnknownErrorView();
        } else {
            if (!currentView[OFFLINE_VIEW])
                showOfflineView();
        }
    }

    private void showOfflineView() {

        if (currentView[RESULTS_VIEW]) {
            currentView[RESULTS_VIEW] = false;
            fadeOutViews(mRecyclerView, null, null, mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton);
        } else if (currentView[INTRO_VIEW]) {
            currentView[INTRO_VIEW] = false;
            fadeOutViews(mIntroTextViewTop, mIntroTextViewBottom, null, mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton);
        } else if (currentView[NO_RESULTS_VIEW]) {
            currentView[NO_RESULTS_VIEW] = false;
            fadeOutViews(mNoResultsTextViewTop, mNoResultsTextViewBottom, null, mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton);
        } else if (currentView[UNKNOWN_ERROR_VIEW]) {
            currentView[UNKNOWN_ERROR_VIEW] = false;
            fadeOutViews(mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null, mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton);
        }

        currentView[OFFLINE_VIEW] = true;
    }

    private void showResultsView() {

        if (currentView[OFFLINE_VIEW]) {
            currentView[OFFLINE_VIEW] = false;
            fadeOutViews(mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton, mRecyclerView, null, null);

        } else if (currentView[INTRO_VIEW]) {
            currentView[INTRO_VIEW] = false;
            fadeOutViews(mIntroTextViewTop, mIntroTextViewBottom, null, mRecyclerView, null, null);
        } else if (currentView[NO_RESULTS_VIEW]) {
            currentView[NO_RESULTS_VIEW] = false;
            fadeOutViews(mNoResultsTextViewTop, mNoResultsTextViewBottom, null, mRecyclerView, null, null);
        } else if (currentView[UNKNOWN_ERROR_VIEW]) {
            currentView[UNKNOWN_ERROR_VIEW] = false;
            fadeOutViews(mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null, mRecyclerView, null, null);
        }

        currentView[RESULTS_VIEW] = true;
    }

    private void showNoResultsView() {

        if (currentView[OFFLINE_VIEW]) {
            currentView[OFFLINE_VIEW] = false;
            fadeOutViews(mNoConnectionTextViewTop, mNoResultsTextViewBottom, mNoConnectionButton, mNoResultsTextViewTop, mNoResultsTextViewBottom, null);
        } else if (currentView[INTRO_VIEW]) {
            currentView[INTRO_VIEW] = false;
            fadeOutViews(mIntroTextViewTop, mIntroTextViewBottom, null, mNoResultsTextViewTop, mNoResultsTextViewBottom, null);
        } else if (currentView[UNKNOWN_ERROR_VIEW]) {
            currentView[UNKNOWN_ERROR_VIEW] = false;
            fadeOutViews(mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null, mNoResultsTextViewTop, mNoResultsTextViewBottom, null);
        } else if (currentView[RESULTS_VIEW]) {
            currentView[RESULTS_VIEW] = false;
            fadeOutViews(mRecyclerView, null, null, mNoResultsTextViewTop, mNoResultsTextViewBottom, null);
        }

        currentView[NO_RESULTS_VIEW] = true;
    }

    private void updateNoResultsTextView(String text) {
        mNoResultsTextViewTop.setText(String.format(getResources().getString(R.string.no_results), text));
    }

    private void showIntroView() {

        if (currentView[OFFLINE_VIEW]) {
            currentView[OFFLINE_VIEW] = false;
            fadeOutViews(mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton, mIntroTextViewTop, mIntroTextViewBottom, null);
        } else if (currentView[UNKNOWN_ERROR_VIEW]) {
            currentView[UNKNOWN_ERROR_VIEW] = false;
            fadeOutViews(mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null, mIntroTextViewTop, mIntroTextViewBottom, null);
        } else if (currentView[RESULTS_VIEW]) {
            currentView[RESULTS_VIEW] = false;
            fadeOutViews(mRecyclerView, null, null, mIntroTextViewTop, mIntroTextViewBottom, null);
        } else if (currentView[NO_RESULTS_VIEW]) {
            currentView[NO_RESULTS_VIEW] = false;
            fadeOutViews(mNoResultsTextViewTop, mNoResultsTextViewBottom, null, mIntroTextViewTop, mIntroTextViewBottom, null);
        }

        currentView[INTRO_VIEW] = true;
    }

    private void showUnknownErrorView() {

        if (currentView[OFFLINE_VIEW]) {
            currentView[OFFLINE_VIEW] = false;
            fadeOutViews(mNoConnectionTextViewTop, mNoConnectionTextViewBottom, mNoConnectionButton, mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null);
        } else if (currentView[INTRO_VIEW]) {
            currentView[INTRO_VIEW] = false;
            fadeOutViews(mIntroTextViewTop, mIntroTextViewBottom, null, mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null);
        } else if (currentView[RESULTS_VIEW]) {
            currentView[RESULTS_VIEW] = false;
            fadeOutViews(mRecyclerView, null, null, mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null);
        } else if (currentView[NO_RESULTS_VIEW]) {
            currentView[NO_RESULTS_VIEW] = false;
            fadeOutViews(mNoResultsTextViewTop, mNoResultsTextViewBottom, null, mUnknownErrorTextViewTop, mUnknownErrorTextViewBottom, null);
        }

        currentView[UNKNOWN_ERROR_VIEW] = true;
    }

    private void fadeOutViews(View vOut1, View vOut2, View vOut3, View vIn1, View vIn2, View vIn3) {

        fadeOutAnimation(vOut1);
        if (vOut2 != null)
            fadeOutAnimation(vOut2);
        if (vOut3 != null)
            fadeOutAnimation(vOut3);

        fadeInAnimation(vIn1);
        if (vIn2 != null)
            fadeInAnimation(vIn2);
        if (vIn3 != null)
            fadeInAnimation(vIn3);
    }


    public void fadeOutAnimation(final View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(mShortAnimationDuration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    public void fadeInAnimation(final View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(mShortAnimationDuration / 2);
        fadeIn.setDuration(mShortAnimationDuration);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeIn);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSearchView.getQuery().length() >= 2) {
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

            outState.putInt("CURRENT_VIEW", currentViewIndex);
            outState.putSerializable("DATA_SET", backupDataSet);

            outState.putInt("LAST_SEARCHED_SEQ_NO", lastSearchedSeqNo);
            outState.putInt("LAST_DISPLAYED_SEQ_NO", lastDisplayedSeqNo);
            outState.putInt("LAST_REQUESTED_PAGE", lastRequestedPage);
            outState.putInt("LAST_DISPLAYED_PAGE", lastDisplayedPage);
            outState.putBoolean("END_REACHED", endReached);

            outState.putBoolean("FIRST_OPEN", false);

        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int currentViewIndex = savedInstanceState.getInt("CURRENT_VIEW");

        Arrays.fill(currentView, false);
        currentView[currentViewIndex] = true;
        Log.d("GNIPPO", "onRestoreInstanceState: "+currentViewIndex);

        if (currentView[OFFLINE_VIEW])
        {
            mIntroTextViewTop.setVisibility(View.GONE);
            mIntroTextViewBottom.setVisibility(View.GONE);
            mNoConnectionTextViewTop.setVisibility(View.VISIBLE);
            mNoConnectionTextViewBottom.setVisibility(View.VISIBLE);
            mNoConnectionButton.setVisibility(View.VISIBLE);
        }
        else if (currentView[NO_RESULTS_VIEW]) {
            updateNoResultsTextView(savedInstanceState.getString("NO_RESULT_SEARCH"));
            booksQuery.setQuery(mSearchView.getQuery());
            mIntroTextViewTop.setVisibility(View.GONE);
            mIntroTextViewBottom.setVisibility(View.GONE);
            mNoResultsTextViewTop.setVisibility(View.VISIBLE);
            mNoResultsTextViewBottom.setVisibility(View.VISIBLE);
        } else if (currentView[RESULTS_VIEW]) {
            backupDataSet = (ArrayList<BookSearchItem>) savedInstanceState.getSerializable("DATA_SET");
            mAdapter = new RecyclerSearchAdapter(backupDataSet, mCurrentPosition.getLatitude(), mCurrentPosition.getLongitude());
            mRecyclerView.setAdapter(mAdapter);
            mIntroTextViewTop.setVisibility(View.GONE);
            mIntroTextViewBottom.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else if (currentView[UNKNOWN_ERROR_VIEW])
        {
            mIntroTextViewTop.setVisibility(View.GONE);
            mIntroTextViewBottom.setVisibility(View.GONE);
            mUnknownErrorTextViewTop.setVisibility(View.VISIBLE);
            mUnknownErrorTextViewBottom.setVisibility(View.VISIBLE);
        }

        lastSearchedSeqNo = savedInstanceState.getInt("LAST_SEARCHED_SEQ_NO");
        lastDisplayedSeqNo = savedInstanceState.getInt("LAST_DISPLAYED_SEQ_NO");
        lastRequestedPage = savedInstanceState.getInt("LAST_REQUESTED_PAGE");
        lastDisplayedPage = savedInstanceState.getInt("LAST_DISPLAYED_PAGE");
        endReached = savedInstanceState.getBoolean("END_REACHED");

    }
}
