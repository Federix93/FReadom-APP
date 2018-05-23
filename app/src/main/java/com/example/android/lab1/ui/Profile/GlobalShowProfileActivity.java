package com.example.android.lab1.ui.Profile;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ViewPagerAdapter;
import com.example.android.lab1.model.User;
import com.example.android.lab1.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class GlobalShowProfileActivity extends AppCompatActivity{

    ImageView mUserImage;
    TextView mUserName;
    ImageView mBackArrow;
    //Toolbar mToolbar;
    TextView mShortBio;
    TextView mRatingNumber;

    FragmentManager mFt = null;

    public static User mUser;
    public static String mUserId;
    public static  TextView mBookNumber;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_show_profile);

        /*
        mToolbar = findViewById(R.id.global_profile_toolbar);

        mToolbar.setTitle(getResources().getString(R.string.title_profile));
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        Utilities.setupStatusBarColor(this);

        mUserImage = findViewById(R.id.global_profile_image);
        mUserName = findViewById(R.id.global_profile_name);
        mBackArrow = findViewById(R.id.back_arrow_global_profile);
        mShortBio = findViewById(R.id.global_bio_text);
        mBookNumber = findViewById(R.id.book_number_global_profile);
        mRatingNumber = findViewById(R.id.rating_number_global_profile);

        mFt = getSupportFragmentManager();

        Intent intent = getIntent();
        mUser = intent.getParcelableExtra("UserObject");
        mUserId = intent.getStringExtra("UserID");

        if (mUser.getImage() != null) {
            Glide.with(this).load(mUser.getImage())
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImage);
        } else {
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImage);
        }
        mUserName.setText(mUser.getUsername());
        if (mUser.getShortBio() != null) {
            mShortBio.setText(mUser.getShortBio());
        }
        mRatingNumber.setText(String.valueOf(mUser.getRating()));

        ViewPager viewPager = findViewById(R.id.viewpager_global_profile);
        setupViewPager(viewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabs = findViewById(R.id.tabs_global_show_profile);
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());
        tabs.setupWithViewPager(viewPager);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*
        mReviewDone.setText("0");
        mOwnerName.setText(String.format(getResources().getString(R.string.book_owner_text), mUser.getUsername()));

        */
    }

    public static User getUser() { return mUser; }

    public static String getUserId () { return mUserId; }

    public static TextView getBookNumber () { return mBookNumber; }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFt);
        adapter.addFragment(new GlobalProfileBooksFragment(), String.format(getResources().getString(R.string.book_owner_text), mUser.getUsername()));
        adapter.addFragment(new GlobalProfileReviewsFragment(), getResources().getString(R.string.profile_reviews_fragment));
        viewPager.setAdapter(adapter);

    }
}
