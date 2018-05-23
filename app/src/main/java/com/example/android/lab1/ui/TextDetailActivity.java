package com.example.android.lab1.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.utils.Utilities;

public class TextDetailActivity extends AppCompatActivity{

    Toolbar mToolbar;
    TextView mBookDescription;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detail);

        mToolbar = findViewById(R.id.toolbar_text_detail_activity);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Utilities.setupStatusBarColor(this);
        mBookDescription = findViewById(R.id.book_detail_description);

        Bundle extras = getIntent().getExtras();
        String bookDescription = null;
        String toolbarTitle = null;
        String shortBio = null;
        if(extras !=null) {
            bookDescription = extras.getString("BookDescription");
            toolbarTitle = extras.getString("Title");
            shortBio = extras.getString("ShortBio");
        }
        if (bookDescription != null)
            mBookDescription.setText(bookDescription);
        if (toolbarTitle != null)
            mToolbar.setTitle(toolbarTitle);
        if (shortBio != null)
            mBookDescription.setText(shortBio);
    }
}
