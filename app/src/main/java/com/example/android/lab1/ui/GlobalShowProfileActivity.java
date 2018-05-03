package com.example.android.lab1.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ProfileBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class GlobalShowProfileActivity extends AppCompatActivity{

    ImageView mImageView;
    TextView mUserName;
    TextView mRating;
    TextView mOwnerName;
    Toolbar mToolbar;

    private User mUser;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_show_profile);

        mToolbar = findViewById(R.id.global_profile_toolbar);

        mToolbar.setTitle(getResources().getString(R.string.app_name));
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        Utilities.setupStatusBarColor(this);

        mImageView = findViewById(R.id.global_profile_image);
        mUserName = findViewById(R.id.global_profile_name);
        mRating = findViewById(R.id.global_profile_rating);
        mOwnerName = findViewById(R.id.global_profile_books_owner);

        RecyclerView recyclerView = findViewById(R.id.rv_books);

        Intent intent = getIntent();
        mUser = (User) intent.getParcelableExtra("UserObject");

        Glide.with(this).load(mUser.getImage())
                .apply(bitmapTransform(new CircleCrop()))
                .into(mImageView);

        mUserName.setText(mUser.getUsername());
        mRating.setText(String.format(getResources().getConfiguration().locale, "%.1f", mUser.getRating()));
        mOwnerName.setText(String.format(getResources().getString(R.string.book_owner_text), mUser.getUsername()));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        ProfileBookAdapter adapter = new ProfileBookAdapter(Book.getRandomBook());
        recyclerView.setAdapter(adapter);
    }

}
