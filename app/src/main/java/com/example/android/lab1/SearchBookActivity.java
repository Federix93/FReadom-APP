package com.example.android.lab1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.algolia.instantsearch.helpers.InstantSearch;
import com.algolia.instantsearch.helpers.Searcher;

public class SearchBookActivity extends AppCompatActivity {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_INDEX_NAME = "books";

    private Searcher searcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);


        searcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);

        searcher.search();

        InstantSearch helper = new InstantSearch(this, searcher);
        helper.search();


    }

    protected void onDestroy() {
        searcher.destroy();
        super.onDestroy();
    }
}
