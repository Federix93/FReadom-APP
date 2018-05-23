package com.example.android.lab1.ui.homepage;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
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
import com.example.android.lab1.ui.Profile.ProfileActivity;
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

    FragmentManager mFragmentManager;
    HomeFragment mHomeFragment;
    YourLibraryFragment mYourLibraryFragment;
    LoanFragment mLoanFragment;
    RequestsFragment mRequestFragment;
    AHBottomNavigation mBottomNavigation;
    ImageView mProfileImage;
    TextView mUsernameTextView;
    TextView mEmailTextView;
    LinearLayout mSideNavLinearLayout;
    private User mUser;

    private static final int HOME_FRAGMENT = 0;
    private static final int YOUR_LIBRARY = 1;
    private static final int LOANS_FRAGMENT = 2;
    private static final int REQUESTS_FRAGMENT = 3;
    private int oldPosition;
    private int comeBackPosition;

    public static final int ADD_BOOK_ACTIVITY = 22847;

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
        mProfileImage = header.findViewById(R.id.global_profile_image);
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
                    mUser = snapshot.toObject(User.class);
                    updateNavigationDrawer();
                }
            });
        }

        mFragmentManager = getSupportFragmentManager();

        mHomeFragment = new HomeFragment();
        mYourLibraryFragment = new YourLibraryFragment();
        mLoanFragment = new LoanFragment();
        mRequestFragment = new RequestsFragment();
        mBottomNavigation = findViewById(R.id.navigation);

        AHBottomNavigationItem homeItem = new AHBottomNavigationItem(getString(R.string.title_home), R.drawable.ic_home_black_24dp);
        AHBottomNavigationItem yourLibrary = new AHBottomNavigationItem(getString(R.string.your_library_fragment), R.drawable.ic_library_books_black_24dp);
        AHBottomNavigationItem loansItem = new AHBottomNavigationItem(getString(R.string.title_loans), R.drawable.ic_compare_arrows_black_24dp);
        AHBottomNavigationItem requestsItem = new AHBottomNavigationItem(getString(R.string.title_requests), R.drawable.ic_dashboard_black_24dp);

        ArrayList<AHBottomNavigationItem> items = new ArrayList<>();

        items.add(homeItem);
        items.add(yourLibrary);
        items.add(loansItem);
        items.add(requestsItem);
        oldPosition = 0;
        mBottomNavigation.addItems(items);
        mBottomNavigation.setBehaviorTranslationEnabled(false);
        mBottomNavigation.setAccentColor(getResources().getColor(R.color.colorSecondaryAccent));
        mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);


        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_frame, mHomeFragment)
                .commit();
        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (wasSelected) {
                    return true;
                } else {
                    switch (position) {
                        case HOME_FRAGMENT:
                            mFragmentManager.beginTransaction().replace(R.id.fragment_frame, mHomeFragment).commit();

                            oldPosition = position;
                            break;

                        case YOUR_LIBRARY:
                            if(mFirebaseAuth.getCurrentUser() == null){
                                Toast.makeText(getApplicationContext(), "Devi essere loggato", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            mFragmentManager.beginTransaction().replace(R.id.fragment_frame, mYourLibraryFragment).commit();

                            oldPosition = position;
                            break;

                        case LOANS_FRAGMENT:
                            mFragmentManager.beginTransaction().replace(R.id.fragment_frame, mLoanFragment).commit();

                            oldPosition = position;
                            break;
                        case REQUESTS_FRAGMENT:
                            mFragmentManager.beginTransaction().replace(R.id.fragment_frame, mRequestFragment).commit();

                            oldPosition = position;
                            break;
                    }
                }
                return true;
            }
        });
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_BOOK_ACTIVITY:
                mBottomNavigation.setCurrentItem(comeBackPosition);
                break;
            case Constants.POSITION_ACTIVITY_REQUEST:
            case Constants.PICK_GENRE:
                mHomeFragment.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }
}