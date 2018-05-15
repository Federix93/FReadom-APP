package com.example.android.lab1.ui.searchbooks;

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
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerSearchAdapter;
import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SearchBookActivity extends AppCompatActivity implements FilterDataPass {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_INDEX_NAME = "books";

    private boolean offlineShown = false;
    private boolean noResultsShown = false;
    private boolean introShown = true;
    private boolean resultsShown = false;

    boolean[] searchByFilters = {true, true, true, true};
    int[] seekBarsFilters = {0, FiltersValues.MAX_POSITION};
    boolean[] orderFilters = {true, false, false, false};

    private FloatingSearchView mSearchView;
    private TextView mIntroTextViewTop;
    private TextView mIntroTextViewBottom;
    private TextView mNoResultsTextViewTop;
    private TextView mNoResultsTextViewBottom;
    private TextView mNoConnectionTextViewTop;
    private TextView mNoConnectionTextViewBottom;
    private AppCompatButton mNoConnectionButton;
    private Index index;
    private RecyclerView mRecyclerView;
    private RecyclerSearchAdapter mAdapter;
    private String lastSearchResult;
    private Query query = new Query();
    private String mCurrentUserId;

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

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);

        setupMenuItemClickListener();
        setupSearchListener();
        setupQueryChangeListener();
        setupLeftItemListener();
        setupNoConnectionButtonClickListener();

        if (savedInstanceState != null) {
            if (savedInstanceState.getString("LAST_SEARCH") != null) {
                lastSearchResult = savedInstanceState.getString("LAST_SEARCH");
                try {
                    mAdapter = new RecyclerSearchAdapter(new JSONArray(lastSearchResult));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mRecyclerView.setAdapter(mAdapter);
            }
        } else {
            mSearchView.setSearchFocused(true);
        }


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
                if (Utilities.isOnline(SearchBookActivity.this)) {
                    if (mSearchView.getQuery().isEmpty() || mSearchView.getQuery().length() < 2) {
                        if (!introShown)
                            showIntroLayout();
                        return;
                    }
                    search(mSearchView.getQuery());
                } else {
                    if (!offlineShown)
                        showOfflineLayout();
                }
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

                if (Utilities.isOnline(SearchBookActivity.this)) {
                    if (currentQuery.isEmpty() || currentQuery.length() < 2) {
                        if (!introShown)
                            showIntroLayout();
                        return;
                    }

                    search(currentQuery);
                } else {
                    if (!offlineShown)
                        showOfflineLayout();
                }

            }
        });
    }

    private void setupQueryChangeListener() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if (Utilities.isOnline(SearchBookActivity.this)) {
                    if (newQuery.isEmpty() || newQuery.length() < 2) {
                        if (!introShown)
                            showIntroLayout();
                        return;
                    }
                    search(newQuery);
                } else {
                    if (!offlineShown)
                        showOfflineLayout();
                }

            }
        });
    }

    private void search(final String searchString) {

        query.setQuery(searchString);
        mSearchView.showProgress();

        index.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject result, AlgoliaException error) {
                JSONArray hits = result.optJSONArray("hits");
                if (hits.length() == 0) {
                    if (!noResultsShown)
                        showNoResultsLayout(searchString);
                    else {
                        updateNoResultsString(searchString);
                    }
                    mSearchView.hideProgress();
                    return;
                } else {
                    if (!resultsShown)
                        showResultsLayout();
                }
                lastSearchResult = hits.toString();
                for (int i = 0; i < hits.length(); i++) {
                    if (hits.optJSONObject(i).optString("uid").equals(mCurrentUserId)) {
                        hits.remove(i);
                        i--;
                    }
                }
                mAdapter = new RecyclerSearchAdapter(hits);
                mRecyclerView.setAdapter(mAdapter);
                mSearchView.hideProgress();
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

        if (!mSearchView.getQuery().isEmpty())
            outState.putString("LAST_SEARCH", lastSearchResult);
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

        query.setFilters("conditions>3");
    }

    private void showOfflineLayout() {
        offlineShown = true;
        noResultsShown = false;
        introShown = false;
        resultsShown = false;

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

    private void showResultsLayout() {
        offlineShown = false;
        noResultsShown = false;
        introShown = false;
        resultsShown = true;

        mNoConnectionTextViewTop.setVisibility(View.GONE);
        mNoConnectionTextViewBottom.setVisibility(View.GONE);
        mNoConnectionButton.setVisibility(View.GONE);
        mIntroTextViewTop.setVisibility(View.GONE);
        mIntroTextViewBottom.setVisibility(View.GONE);
        mNoResultsTextViewTop.setVisibility(View.GONE);
        mNoResultsTextViewBottom.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoResultsLayout(String searchString) {
        offlineShown = false;
        noResultsShown = true;
        introShown = false;
        resultsShown = false;

        mRecyclerView.setVisibility(View.GONE);
        if (mRecyclerView.getAdapter() == null)
            mRecyclerView.swapAdapter(null, true);
        mNoConnectionTextViewTop.setVisibility(View.GONE);
        mNoConnectionTextViewBottom.setVisibility(View.GONE);
        mNoConnectionButton.setVisibility(View.GONE);
        mIntroTextViewTop.setVisibility(View.GONE);
        mIntroTextViewBottom.setVisibility(View.GONE);
        mNoResultsTextViewTop.setText(String.format(getResources().getString(R.string.no_results), searchString));
        mNoResultsTextViewTop.setVisibility(View.VISIBLE);
        mNoResultsTextViewBottom.setVisibility(View.VISIBLE);
    }

    private void updateNoResultsString(String searchString) {
        mNoResultsTextViewTop.setText(String.format(getResources().getString(R.string.no_results), searchString));
    }

    private void showIntroLayout() {
        offlineShown = false;
        noResultsShown = false;
        introShown = true;
        resultsShown = false;

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
}
