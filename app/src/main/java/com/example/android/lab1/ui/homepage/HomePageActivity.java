package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager mFragmentManager;
    HomeFragment mHomeFragment;
    DashboardFragment mDashboardFragment;
    ProfileFragment mProfileFragment;
    AHBottomNavigation mBottomNavigation;
    ImageView mProfileImage;
    TextView mUsernameTextView;
    TextView mEmailTextView;
    LinearLayout mSideNavLinearLayout;

    private static final int HOME_FRAGMENT = 0;
    private static final int DASH_FRAGMENT = 1;
    private static final int PROFILE_FRAGMENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar_home_page_activity);

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

        header.setBackgroundResource(R.drawable.background);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        if (mFirebaseAuth.getCurrentUser() != null) {
            final DocumentReference docRef = firebaseFirestore.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    User user = snapshot.toObject(User.class);
                    updateNavigationDrawer(user);
                }
            });
        }

        mFragmentManager = getSupportFragmentManager();

        mHomeFragment = new HomeFragment();
        mDashboardFragment = new DashboardFragment();
        mProfileFragment = new ProfileFragment();
        mBottomNavigation = findViewById(R.id.navigation);

        AHBottomNavigationItem homeItem = new AHBottomNavigationItem(getString(R.string.title_home), R.drawable.ic_home_black_24dp);
        AHBottomNavigationItem dashItem = new AHBottomNavigationItem(getString(R.string.title_dashboard), R.drawable.ic_dashboard_black_24dp);
        AHBottomNavigationItem profileItem = new AHBottomNavigationItem(getString(R.string.title_profile), R.drawable.ic_person_black_24dp);

        ArrayList<AHBottomNavigationItem> items = new ArrayList<>();

        items.add(homeItem);
        items.add(dashItem);
        items.add(profileItem);

        mBottomNavigation.addItems(items);
        mBottomNavigation.setBehaviorTranslationEnabled(false);

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (wasSelected) {
                    return true;
                } else {
                    android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
                    switch (position) {
                        case HOME_FRAGMENT:
                            ft.replace(R.id.fragment_frame, mHomeFragment).commit();
                            break;

                        case DASH_FRAGMENT:
                            ft.replace(R.id.fragment_frame, mDashboardFragment).commit();
                            break;

                        case PROFILE_FRAGMENT:
                            ft.replace(R.id.fragment_frame, mProfileFragment).commit();
                            break;

                    }
                }

                return true;
            }
        });

        if (savedInstanceState == null) {

            if (getIntent().getBooleanExtra("ApplyChanges", false)) {

                mFragmentManager.beginTransaction()
                        .replace(R.id.fragment_frame, mProfileFragment)
                        .commit();

                mBottomNavigation.setCurrentItem(PROFILE_FRAGMENT);
            } else {
                mFragmentManager.beginTransaction()
                        .replace(R.id.fragment_frame, mHomeFragment)
                        .commit();
            }
        }
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

        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_logout) {

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

}
