package com.example.android.lab1.ui.homepage;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.LoadBookActivity;
import com.example.android.lab1.ui.ProfileActivity;
import com.example.android.lab1.ui.SignInActivity;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;
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

import java.util.ArrayList;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int ADD_BOOK_ACTIVITY = 22847;
    private static final String CURRENT_FRAGMENT = "HFK";
    private static final String DASHBOARD_FRAGMENT = "DASHBOARD_FRAG";
    private static final int HOME_FRAGMENT = 0;
    private static final int ADD_BOOK = 1;
    private static final int DASH_FRAGMENT = 2;
    FragmentManager mFragmentManager;
    HomeFragment mHomeFragment;
    DashboardFragment mDashboardFragment;
    AHBottomNavigation mBottomNavigation;
    ImageView mProfileImage;
    TextView mUsernameTextView;
    TextView mEmailTextView;
    LinearLayout mSideNavLinearLayout;
    private int oldPosition;
    private int comeBackPosition;
    private Fragment mCurrentFragment;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar_home_page_activity);

        Utilities.setupStatusBarColor(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        mProfileImage = header.findViewById(R.id.profile_image);
        mUsernameTextView = header.findViewById(R.id.name_text_nav_drawer);
        mEmailTextView = header.findViewById(R.id.email_text_nav_drawer);
        mSideNavLinearLayout = header.findViewById(R.id.header_nav_drawer);

        //header.setBackgroundResource(R.drawable.background);

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        if (mFirebaseAuth.getCurrentUser() != null) {
            final DocumentReference docRef = firebaseFirestore.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                    User user = snapshot.toObject(User.class);
                    updateNavigationDrawer(user);
                }
            });
        }

        mFragmentManager = getSupportFragmentManager();


        mBottomNavigation = findViewById(R.id.navigation);

        AHBottomNavigationItem homeItem = new AHBottomNavigationItem(getString(R.string.title_home), R.drawable.ic_home_black_24dp);
        AHBottomNavigationItem addBook = new AHBottomNavigationItem(getString(R.string.add_book_fragment_title), R.drawable.ic_add_box_black_24dp);
        AHBottomNavigationItem dashItem = new AHBottomNavigationItem(getString(R.string.title_dashboard), R.drawable.ic_dashboard_black_24dp);

        ArrayList<AHBottomNavigationItem> items = new ArrayList<>();

        items.add(homeItem);
        items.add(addBook);
        items.add(dashItem);
        oldPosition = 0;
        mBottomNavigation.addItems(items);
        mBottomNavigation.setBehaviorTranslationEnabled(false);
        mBottomNavigation.setAccentColor(getResources().getColor(R.color.colorSecondaryAccent));

        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_FRAGMENT)) {
            mCurrentFragment = mFragmentManager.getFragment(savedInstanceState,
                    CURRENT_FRAGMENT);
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_frame, mCurrentFragment)
                    .commit();
        } else {
            mHomeFragment = new HomeFragment();
            mDashboardFragment = new DashboardFragment();
            mCurrentFragment = mHomeFragment;
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_frame, mCurrentFragment)
                    .commit();
        }

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (wasSelected) {
                    return true;
                } else {
                    switch (position) {
                        case HOME_FRAGMENT:
                            if (oldPosition != ADD_BOOK) {
                                if (mHomeFragment == null) // may be null if dashboard was selected and user rotated device
                                    mHomeFragment = new HomeFragment();
                                mFragmentManager.beginTransaction().replace(R.id.fragment_frame, mHomeFragment).commit();
                                mCurrentFragment = mHomeFragment;
                            }

                            oldPosition = position;
                            break;

                        case ADD_BOOK:
                            if (mFirebaseAuth.getCurrentUser() == null) {
                                Toast.makeText(getApplicationContext(), "Devi essere loggato", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            comeBackPosition = oldPosition;
                            oldPosition = position;
                            Intent intent = new Intent(getApplicationContext(), LoadBookActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivityForResult(intent, ADD_BOOK_ACTIVITY);
                            break;

                        case DASH_FRAGMENT:
                            if (oldPosition != ADD_BOOK) {
                                if (mDashboardFragment == null)
                                    mDashboardFragment = new DashboardFragment();
                                mFragmentManager.beginTransaction().replace(R.id.fragment_frame, mDashboardFragment).commit();
                                mCurrentFragment = mDashboardFragment;
                            }
                            oldPosition = position;
                            break;
                    }
                }

                return true;
            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, CURRENT_FRAGMENT, mCurrentFragment);
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

    private void updateNavigationDrawer(User user) {

        if (user != null) {
            if (user.getImage() != null) {
                Glide.with(getApplicationContext())
                        .load(user.getImage())
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mProfileImage);

            } else {
                Glide.with(getApplicationContext())
                        .load(R.drawable.ic_account_circle_black_24dp)
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mProfileImage);
            }
            mUsernameTextView.setText(user.getUsername());
            mEmailTextView.setText(user.getEmail());
        } else {
            Glide.with(getApplicationContext())
                    .load(R.drawable.ic_account_circle_black_24dp)
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mProfileImage);

            mUsernameTextView.setText(R.string.no_name_nav_drawer);
            mEmailTextView.setText(R.string.no_email_nav_drawer);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_BOOK_ACTIVITY:
                mBottomNavigation.setCurrentItem(comeBackPosition);
                break;
            case Constants.POSITION_ACTIVITY_REQUEST:
            case Constants.PICK_GENRE:
                mCurrentFragment.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }
}
