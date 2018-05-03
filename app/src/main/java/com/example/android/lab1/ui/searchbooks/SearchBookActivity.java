package com.example.android.lab1.ui.searchbooks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchBookActivity extends AppCompatActivity {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_INDEX_NAME = "books";
    
    private FloatingSearchView mSearchView;
    private Index index;
    private Query query;
    private RecyclerView mRecyclerView;
    private RecyclerSearchAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);

        mSearchView = findViewById(R.id.floating_search_view);
        mRecyclerView = findViewById(R.id.results_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);

        setupMenuItemCliclListener();
        setupSearchListener();
        setupQueryChangeListener();

    }

    private void setupMenuItemCliclListener()
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
                                    new Integer[]{1},
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

            }
        });
    }

    private void setupQueryChangeListener()
    {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if(newQuery.isEmpty())
                    return;

                query = new Query();
                query.setQuery(newQuery);


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


//                query.setRestrictSearchableAttributes("title");

                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject result, AlgoliaException error) {
                        JSONArray hits = result.optJSONArray("hits");
                        mAdapter = new RecyclerSearchAdapter(hits);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
