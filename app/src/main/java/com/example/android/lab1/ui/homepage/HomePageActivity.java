package com.example.android.lab1.ui.homepage;

import android.animation.Animator;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.LoadBookActivity;
import com.example.android.lab1.ui.profile.ProfileActivity;
import com.example.android.lab1.ui.SignInActivity;
import com.example.android.lab1.ui.searchbooks.SearchBookActivity;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.UserViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AHBottomNavigation mBottomNavigation;

    ImageView mProfileImage;
    TextView mUsernameTextView;
    TextView mEmailTextView;
    LinearLayout mSideNavLinearLayout;
    private User mUser;

    private static final int HOME_FRAGMENT = 0;
    private static final int YOUR_LIBRARY_FRAGMENT = 1;
    private static final int LOANS_FRAGMENT = 2;
    private static final int REQUESTS_FRAGMENT = 3;

    private FragmentAdapter mFragmentAdapter;
    private TabFragment mCurrentFragment;
    private AHBottomNavigationViewPager mBottomNavigationViewPager;
    private FloatingActionButton mFAB;
    private Toolbar mToolbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mToolbar = findViewById(R.id.toolbar_home_page_activity);

        Utilities.setupStatusBarColor(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        mProfileImage = header.findViewById(R.id.global_profile_image);
        mUsernameTextView = header.findViewById(R.id.name_text_nav_drawer);
        mEmailTextView = header.findViewById(R.id.email_text_nav_drawer);
        mSideNavLinearLayout = header.findViewById(R.id.header_nav_drawer);
        mBottomNavigation = findViewById(R.id.navigation);
        mBottomNavigationViewPager = findViewById(R.id.fragment_frame);
        mFAB = findViewById(R.id.floating_action_button_library);

        //header.setBackgroundResource(R.drawable.background);

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        if (mFirebaseAuth.getCurrentUser() != null) {
            UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
            final LiveData<User> liveData = userViewModel.getSnapshotLiveData();

            liveData.observe(this, new Observer<User>() {
                @Override
                public void onChanged(@Nullable User user) {
                    Log.d("LULLO", "UpdateNavigationDrawer");
                    mUser = user;
                    updateNavigationDrawer();
                }
            });
            /*final DocumentReference docRef = firebaseFirestore.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                    mUser = snapshot.toObject(User.class);
                    Log.d("LULLO", "UpdateNavigationDrawer");
                    updateNavigationDrawer();
                }
            });*/
        }

        mToolbar.setTitle(getString(R.string.toolbar_title_home));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_home);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                switch (clickedId) {
                    case R.id.action_search:
                        Intent intent = new Intent(HomePageActivity.this, SearchBookActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        AHBottomNavigationItem homeItem = new AHBottomNavigationItem(getString(R.string.title_home), R.drawable.ic_home_black_24dp);
        AHBottomNavigationItem yourLibrary = new AHBottomNavigationItem(getString(R.string.your_library_fragment), R.drawable.ic_library_books_black_24dp);
        AHBottomNavigationItem loansItem = new AHBottomNavigationItem(getString(R.string.title_loans), R.drawable.ic_compare_arrows_black_24dp);
        AHBottomNavigationItem requestsItem = new AHBottomNavigationItem(getString(R.string.title_requests), R.drawable.ic_dashboard_black_24dp);

        mBottomNavigation.addItem(homeItem);
        mBottomNavigation.addItem(yourLibrary);
        mBottomNavigation.addItem(loansItem);
        mBottomNavigation.addItem(requestsItem);

        mBottomNavigation.setBehaviorTranslationEnabled(false);
        mBottomNavigation.setAccentColor(getResources().getColor(R.color.colorSecondaryAccent));
        mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, LoadBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (mCurrentFragment == null) {
                    mCurrentFragment = mFragmentAdapter.getCurrentFragment();
                }

                if (wasSelected) {
                    mCurrentFragment.refresh();
                    return true;
                }

                if (mCurrentFragment != null) {
                    mCurrentFragment.willBeHidden();
                }

                mBottomNavigationViewPager.setCurrentItem(position, false);

                if (mCurrentFragment == null) {
                    return true;
                }

                mCurrentFragment = mFragmentAdapter.getCurrentFragment();
                mCurrentFragment.willBeDisplayed();

                if(position == HOME_FRAGMENT)
                {
                    mToolbar.setTitle(getString(R.string.toolbar_title_home));
                    mToolbar.getMenu().clear();
                    mToolbar.inflateMenu(R.menu.fragment_home);
                    mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int clickedId = item.getItemId();
                            switch (clickedId) {
                                case R.id.action_search:
                                    Intent intent = new Intent(HomePageActivity.this, SearchBookActivity.class);
                                    startActivity(intent);
                                    break;
                            }
                            return true;
                        }
                    });
                }

                if (position == YOUR_LIBRARY_FRAGMENT) {
                    mToolbar.setTitle(R.string.your_library_fragment);
                    mToolbar.getMenu().clear();
                    fabAppears();
                }
                else {
                    if (mFAB.getVisibility() == View.VISIBLE) {
                        fabDisappears();
                    }
                }

                if(position == LOANS_FRAGMENT)
                {
                    mToolbar.setTitle(R.string.title_loans);
                    mToolbar.getMenu().clear();
                }

                if(position == REQUESTS_FRAGMENT)
                {
                    mToolbar.setTitle(R.string.title_requests);
                    mToolbar.getMenu().clear();
                }

                return true;

            }
        });


        mBottomNavigationViewPager.setOffscreenPageLimit(4);
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        mBottomNavigationViewPager.setAdapter(mFragmentAdapter);

        mCurrentFragment = mFragmentAdapter.getCurrentFragment();


    }

    private void fabAppears()
    {
        mFAB.setVisibility(View.VISIBLE);
        mFAB.setAlpha(0f);
        mFAB.setScaleX(0f);
        mFAB.setScaleY(0f);
        mFAB.animate()
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFAB.animate()
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }
    private void fabDisappears()
    {
        mFAB.animate()
                .alpha(0)
                .scaleX(0)
                .scaleY(0)
                .setDuration(300)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFAB.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mFAB.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Toast.makeText(this, "Function not Implemented", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("user", mUser);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                            finish();
                        }
                    });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateNavigationDrawer() {

        if (mUser != null) {
            if (mUser.getImage() != null) {
                Glide.with(getApplicationContext())
                        .load(mUser.getImage())
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mProfileImage);

            } else {
                Glide.with(getApplicationContext())
                        .load(R.drawable.ic_account_circle_black_24dp)
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mProfileImage);
            }
            mUsernameTextView.setText(mUser.getUsername());
            mEmailTextView.setText(mUser.getEmail());
        } else {
            Glide.with(getApplicationContext())
                    .load(R.drawable.ic_account_circle_black_24dp)
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mProfileImage);

            mUsernameTextView.setText(R.string.no_name_nav_drawer);
            mEmailTextView.setText(R.string.no_email_nav_drawer);

        }
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case Constants.POSITION_ACTIVITY_REQUEST:
//                break;
//            case Constants.PICK_GENRE:
//                Log.d(TAG, "onActivityResult: passo al fragment");
//                mCurrentFragment.onActivityResult(requestCode, resultCode, data);
//                break;
//        }
//
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mFragmentAdapter.getCurrentFragment().getFragmentType() == YOUR_LIBRARY_FRAGMENT)
        {
            outState.putBoolean("SHOW_FAB", true);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getBoolean("SHOW_FAB", false))
        {
            mFAB.setVisibility(View.VISIBLE);
        }
    }
}

