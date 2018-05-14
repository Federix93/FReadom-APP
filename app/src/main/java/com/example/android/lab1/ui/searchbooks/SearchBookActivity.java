package com.example.android.lab1.ui.searchbooks;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SearchBookActivity extends AppCompatActivity {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_INDEX_NAME = "books";

    boolean[] searchByFilters = {true, true, true, true};
    int[] seekBarsFilters = {0, 100};
    boolean[] orderFilters = {true, false, false, false};

    int ratingPreference = 0;
    int oldRatingPreference = 0;

    private FloatingSearchView mSearchView;
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
                search(mSearchView.getQuery());
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

                if (currentQuery.isEmpty()) {
                    mRecyclerView.swapAdapter(null, true);
                    return;
                }

                search(currentQuery);

            }
        });
    }

    private void setupQueryChangeListener() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if (newQuery.isEmpty()) {
                    mRecyclerView.swapAdapter(null, true);
                    return;
                }

                search(newQuery);
            }
        });
    }

    private void search(String searchString) {
        query.setQuery(searchString);

        if (Utilities.isOnline(SearchBookActivity.this)) {
            mSearchView.showProgress();
            mNoConnectionTextViewTop.setVisibility(View.GONE);
            mNoConnectionTextViewBottom.setVisibility(View.GONE);
            mNoConnectionButton.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            index.searchAsync(query, new CompletionHandler() {
                @Override
                public void requestCompleted(JSONObject result, AlgoliaException error) {
                    JSONArray hits = result.optJSONArray("hits");
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
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoConnectionTextViewTop.setVisibility(View.VISIBLE);
            mNoConnectionTextViewBottom.setVisibility(View.VISIBLE);
            mNoConnectionButton.setVisibility(View.VISIBLE);
        }
    }

    private void showDialog() {

        Bundle filterData = new Bundle();
        filterData.putBooleanArray("search_by", searchByFilters);
        filterData.putIntArray("seek_bars", seekBarsFilters);
        filterData.putBooleanArray("order_filters", orderFilters);

        FragmentManager fm = getSupportFragmentManager();
        FiltersFragment filtersFragment = FiltersFragment.newInstance(filterData);
        filtersFragment.show(fm, "filters_fragment");

//        SeekBar ratingPreferenceBar = new SeekBar(this);
//        ratingPreferenceBar.setMax(5);
//        ratingPreferenceBar.setKeyProgressIncrement(1);
//        ratingPreferenceBar.setProgress(ratingPreference);
//
//        oldCheckedItems = checkedItems.clone();
//        String[] multiChoiceItems = getResources().getStringArray(R.array.search_filters);
//        new AlertDialog.Builder(SearchBookActivity.this)
//                .setTitle(getResources().getString(R.string.search_by))
//                .setMultiChoiceItems(multiChoiceItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int index, boolean isChecked) {
//                        if (!checkedItems[0] && !checkedItems[1] && !checkedItems[2] && !checkedItems[3]) {
//                            final AlertDialog alert = (AlertDialog) dialog;
//                            final ListView list = alert.getListView();
//                            list.setItemChecked(index, true);
//                            Toast.makeText(SearchBookActivity.this, getResources().getString(R.string.min_selection), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .setView(ratingPreferenceBar)
//                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        List<String> searchFields = new ArrayList<>();
//                        if (checkedItems[0]) {
//                            searchFields.add("title");
//                        }
//                        if (checkedItems[1]) {
//                            searchFields.add("author");
//                        }
//                        if (checkedItems[2]) {
//                            searchFields.add("publisher");
//                        }
//                        if (checkedItems[3]) {
////                                        searchFields.add("tags");
//                        }
//
//                        query.setRestrictSearchableAttributes(searchFields.toArray(new String[searchFields.size()]));
//                    }
//                })
//                .setNegativeButton(getResources().getString(R.string.negative_button_dialog), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        checkedItems = oldCheckedItems;
//                    }
//                })
//                .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        checkedItems = oldCheckedItems;
//                    }
//                })
//                .show();
//
//        ratingPreferenceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                Toast.makeText(SearchBookActivity.this, "Value of " + progress, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (!mSearchView.getQuery().isEmpty())
            outState.putString("LAST_SEARCH", lastSearchResult);
        super.onSaveInstanceState(outState);
    }
}
