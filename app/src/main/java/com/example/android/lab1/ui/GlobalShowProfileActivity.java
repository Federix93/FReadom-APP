package com.example.android.lab1.ui;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ProfileBookAdapter;
import com.example.android.lab1.model.Book;

public class GlobalShowProfileActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_show_profile);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.rv_books);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        ProfileBookAdapter adapter = new ProfileBookAdapter(Book.getRandomBook());
        recyclerView.setAdapter(adapter);
    }

}
