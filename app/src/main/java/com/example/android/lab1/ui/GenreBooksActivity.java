package com.example.android.lab1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.utils.Utilities;

public class GenreBooksActivity extends AppCompatActivity {

    public static final String SELECTED_GENRE = "SELECTED_GENRE";
    private static final int NO_GENRE_SELECTED = -1;
    Toolbar mToolbar;
    RecyclerView mGenreListRecyclerView;

    public static boolean isValidGenre(int i) {
        return i >= 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_books);

        Utilities.setupStatusBarColor(this);

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
        mGenreListRecyclerView = findViewById(R.id.genre_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mGenreListRecyclerView.setLayoutManager(linearLayoutManager);
        mGenreListRecyclerView.setHasFixedSize(true);
        mGenreListRecyclerView.setAdapter(new GenreAdapterSelect(getResources().getStringArray(R.array.genre)));
    }

    class GenreAdapterSelect extends RecyclerView.Adapter<GenreAdapterSelect.GenreVH> {

        private String[] mGenres;

        public GenreAdapterSelect(String[] genres) {
            this.mGenres = genres;
        }

        @NonNull
        @Override
        public GenreVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new GenreVH(layoutInflater.inflate(R.layout.item_book_genre,
                    parent,
                    false));
        }

        @Override
        public void onBindViewHolder(@NonNull GenreVH holder, int position) {
            if (position == 0)
                holder.genreTextView.setText(R.string.no_genre_in_particular);
            else
                holder.genreTextView.setText(mGenres[position - 1]);
        }

        @Override
        public int getItemCount() {
            return mGenres.length + 1;
        }

        class GenreVH extends RecyclerView.ViewHolder {
            public TextView genreTextView;

            public GenreVH(View itemView) {
                super(itemView);
                genreTextView = itemView.findViewById(R.id.genre_text_view);
                genreTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int selectedGenre = getAdapterPosition();
                        if (selectedGenre < 0)
                            selectedGenre = NO_GENRE_SELECTED;
                        else
                            selectedGenre--;
                        Intent i = new Intent();
                        i.putExtra(SELECTED_GENRE, selectedGenre);
                        setResult(RESULT_OK, i);
                        finish();
                    }
                });
            }
        }
    }
}


