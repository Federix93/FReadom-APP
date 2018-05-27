package com.example.android.lab1.ui.profile;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ViewPagerAdapter;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.TextDetailActivity;
import com.example.android.lab1.utils.Utilities;

import static android.view.View.GONE;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class GlobalShowProfileActivity extends AppCompatActivity{

    ImageView mUserImage;
    TextView mUserName;
    ImageView mBackArrow;
    ImageView mRatingStar;
    TextView mShortBio;
    TextView mRatingText;
    TextView mViewMoreText;

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
        mRatingText = findViewById(R.id.rating_text_global_profile);
        mRatingStar = findViewById(R.id.global_rating_star);
        mViewMoreText = findViewById(R.id.global_view_more);

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
        if (Float.compare(mUser.getRating(), 0) != 0) {
            mRatingText.setText(String.format(getResources().getString(R.string.rating_global_profile), mUser.getRating()));
            Glide.with(this).load(R.drawable.ic_star_orange_24dp).into(mRatingStar);

        } else {
            mRatingText.setText(getResources().getString(R.string.not_rated_yet));
            Glide.with(this).load(R.drawable.ic_star_empty_24dp).into(mRatingStar);
        }

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


        ViewTreeObserver vto = mShortBio.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Layout l = mShortBio.getLayout();
                if ( l != null){
                    int lines = l.getLineCount();
                    if ( lines > 0)
                        if ( l.getEllipsisCount(lines-1) > 0) {
                            mViewMoreText.setVisibility(View.VISIBLE);
                            mViewMoreText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), TextDetailActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("Title", mUser.getUsername());
                                    intent.putExtra("ShortBio", mUser.getShortBio());
                                    startActivity(intent);
                                }
                            });
                            mShortBio.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), TextDetailActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("Title", mUser.getUsername());
                                    intent.putExtra("ShortBio", mUser.getShortBio());
                                    startActivity(intent);
                                }
                            });
                        } else {
                            mViewMoreText.setVisibility(GONE);
                        }
                }
            }
        });


    }

    public static User getUser() { return mUser; }

    public static String getUserId () { return mUserId; }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFt);
        adapter.addFragment(new GlobalProfileBooksFragment(), getResources().getString(R.string.books));
        adapter.addFragment(new GlobalProfileReviewsFragment(), getResources().getString(R.string.profile_reviews_fragment));
        viewPager.setAdapter(adapter);

    }
}
