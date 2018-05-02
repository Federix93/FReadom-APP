package com.example.android.lab1.ui;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.GenreBooksAdapter;
import com.example.android.lab1.model.GenreBook;

import java.util.ArrayList;

public class GenreBooksActivity extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView mGenreRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_books);

        mToolbar = findViewById(R.id.genre_toolbar_activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setTitle(getString(R.string.toolbar_title_home));
            mToolbar.getMenu().clear();
            mToolbar.inflateMenu(R.menu.fragment_home);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int clickedId = item.getItemId();
                    switch (clickedId) {
                        case R.id.action_search:
                            Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                }
                });
        }
        mGenreRecyclerView = findViewById(R.id.genre_recycler_view);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mGenreRecyclerView.setLayoutManager(mLayoutManager);
        mGenreRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mGenreRecyclerView.setHasFixedSize(true);
        mGenreRecyclerView.setAdapter(new GenreBooksAdapter(this));
    }
}
