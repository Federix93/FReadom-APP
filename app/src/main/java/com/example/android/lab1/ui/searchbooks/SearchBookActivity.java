package com.example.android.lab1.ui.searchbooks;

import android.app.SearchManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.algolia.instantsearch.helpers.InstantSearch;
import com.algolia.instantsearch.helpers.Searcher;
import com.example.android.lab1.R;

public class SearchBookActivity extends AppCompatActivity {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_INDEX_NAME = "books";

    private Toolbar mToolbar;
    private Searcher searcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);

        mToolbar = findViewById(R.id.toolbar_search_books);
        //mToolbar.inflateMenu();




//        searcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);
//
//        searcher.search();

//        InstantSearch helper = new InstantSearch(this, searcher);
//        helper.search();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_books, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search_button_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getApplicationContext(), "Search: " +query, Toast.LENGTH_SHORT).show();
                /*if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }*/

                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                //Toast.makeText(getActivity(), "Text now is " +s, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        return true;
    }

    protected void onDestroy() {
      //  searcher.destroy();
        super.onDestroy();
    }
}
