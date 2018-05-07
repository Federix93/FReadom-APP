package com.example.android.lab1.ui.searchbooks;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchBookActivity extends AppCompatActivity {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_INDEX_NAME = "books";

    private FloatingSearchView mSearchView;
    private TextView mNoConnectionTextViewTop;
    private TextView mNoConnectionTextViewBottom;
    private AppCompatButton mNoConnectionButton;
    private Index index;
    private RecyclerView mRecyclerView;
    private RecyclerSearchAdapter mAdapter;
    private String lastSearchResult;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);

        setupMenuItemClickListener();
        setupSearchListener();
        setupQueryChangeListener();
        setupLeftItemListener();
        setupNoConnectionButtonClickListener();

        if(savedInstanceState != null)
        {
            if(savedInstanceState.getString("LAST_SEARCH") != null)
            {
                lastSearchResult = savedInstanceState.getString("LAST_SEARCH");
                try {
                    mAdapter = new RecyclerSearchAdapter(new JSONArray(lastSearchResult));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mRecyclerView.setAdapter(mAdapter);
            }
        }
        else
        {
            mSearchView.setSearchFocused(true);
        }


    }

    private void setupLeftItemListener() {

        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                if(mSearchView.isFocused())
                    mSearchView.setSearchFocused(false);
                else
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

    private void setupMenuItemClickListener()
    {
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                int itemID = item.getItemId();
                if(itemID == R.id.action_filter)
                {
                    new MaterialDialog.Builder(SearchBookActivity.this)
                            .title(R.string.filter_dialog_title)
                            .items(R.array.searchFilters)
                            .itemsCallbackMultiChoice(
                                    new Integer[]{0,1,2,3},
                                    new MaterialDialog.ListCallbackMultiChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                            boolean allowSelectionChange =
                                                    which.length
                                                            >= 1; // selection count must stay above 1, the new (un)selection is included
                                            // in the which array
                                            if (!allowSelectionChange) {
                                                Toast.makeText(SearchBookActivity.this, getResources().getString(R.string.min_selection), Toast.LENGTH_LONG).show();

                                            }
                                            return allowSelectionChange;
                                        }
                                    })
                            .positiveText(R.string.confirm)
                            .alwaysCallMultiChoiceCallback() // the callback will always be called, to check if
                            // (un)selection is still allowed
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void setupSearchListener()
    {
        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {

                if(currentQuery.isEmpty())
                {
                    mRecyclerView.swapAdapter(null, true);
                    return;
                }

                search(currentQuery);

            }
        });
    }

    private void setupQueryChangeListener()
    {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if(newQuery.isEmpty())
                {
                    mRecyclerView.swapAdapter(null, true);
                    return;
                }

                search(newQuery);
            }
        });
    }

    private void search(String searchString)
    {
        Query query = new Query();
        query.setQuery(searchString);


//                List<String> aaa = new ArrayList<>();
//
//                aaa.add("title");
//                aaa.add("tags");
//
//                String[] we = new String[aaa.size()];
//
//                aaa.toArray(we);
//
//                for(int i=0; i<aaa.size(); i++)
//                    Log.d("TESTBOOKS", we[i]);


//                query.setRestrictSearchableAttributes(we);

        if(Utilities.isOnline(SearchBookActivity.this)) {
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
                    mAdapter = new RecyclerSearchAdapter(hits);
                    mRecyclerView.setAdapter(mAdapter);
                    mSearchView.hideProgress();
                }
            });
        }
        else
        {
            mRecyclerView.setVisibility(View.GONE);
            mNoConnectionTextViewTop.setVisibility(View.VISIBLE);
            mNoConnectionTextViewBottom.setVisibility(View.VISIBLE);
            mNoConnectionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if(!mSearchView.getQuery().isEmpty())
            outState.putString("LAST_SEARCH", lastSearchResult);
        super.onSaveInstanceState(outState);
    }
}
