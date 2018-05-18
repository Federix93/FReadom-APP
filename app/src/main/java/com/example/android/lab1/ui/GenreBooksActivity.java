package com.example.android.lab1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.lab1.R;

public class GenreBooksActivity extends AppCompatActivity {

    public static final String SELECTED_GENRE = "SELECTED_GENRE";
    private static final int NO_GENRE_SELECTED = -1;
    Toolbar mToolbar;
    ListView mGenreListView;

    public static boolean isValidGenre(int i) {
        return i >= 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_books);

        mToolbar = findViewById(R.id.genre_toolbar_activity);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        mToolbar.setTitle(R.string.choose_genre);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        setSupportActionBar(mToolbar);
        // }
        mGenreListView = findViewById(R.id.genre_recycler_view);
        mGenreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent result = new Intent();
                result.putExtra(SELECTED_GENRE, position - 1);
                setResult(RESULT_OK, result);
                finish();
            }
        });
        TextView noGenreTextView = (TextView) getLayoutInflater()
                .inflate(R.layout.item_book_genre, null, false);
        noGenreTextView.setText(R.string.no_genre_in_particular);
        noGenreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                mGenreListView.setOnItemClickListener(null);
                Intent i = new Intent();
                i.putExtra(SELECTED_GENRE, NO_GENRE_SELECTED);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        mGenreListView.addHeaderView(noGenreTextView);
        mGenreListView.setAdapter(ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.genre,
                R.layout.item_book_genre));
    }
}
