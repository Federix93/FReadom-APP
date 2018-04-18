package com.example.android.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private static final int HOME_FRAGMENT = 0;
    private static final int DASH_FRAGMENT = 1;
    private static final int PROFILE_FRAGMENT = 2;

    AHBottomNavigation mBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        mBottomNavigation = findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem homeItem = new AHBottomNavigationItem(getString(R.string.title_home), R.drawable.ic_home_black_24dp);
        AHBottomNavigationItem dashItem = new AHBottomNavigationItem(getString(R.string.title_dashboard), R.drawable.ic_dashboard_black_24dp);
        AHBottomNavigationItem profileItem = new AHBottomNavigationItem(getString(R.string.title_profile), R.drawable.ic_person_black_24dp);

        ArrayList<AHBottomNavigationItem> items = new ArrayList<>();

        items.add(homeItem);
        items.add(dashItem);
        items.add(profileItem);

        mBottomNavigation.addItems(items);
        mBottomNavigation.setBehaviorTranslationEnabled(false);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (wasSelected)
                {
                    return true;
                }
                else
                {
                    android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                    switch (position)
                    {
                        case HOME_FRAGMENT:
                            HomeFragment homeFragment = new HomeFragment();
                            ft.replace(R.id.fragment_frame, homeFragment).commit();
                            break;

                        case DASH_FRAGMENT:
                            DashboardFragment dashboardFragment = new DashboardFragment();
                            ft.replace(R.id.fragment_frame, dashboardFragment).commit();
                            break;

                        case PROFILE_FRAGMENT:
                            ProfileFragment profileFragment = new ProfileFragment();
                            ft.replace(R.id.fragment_frame, profileFragment).commit();
                            break;

                    }
                }

                return true;
            }
        });




        // Only create new fragments when there is no previously saved state
        if(savedInstanceState == null) {

            if (getIntent().getBooleanExtra("ApplyChanges", false))
            {
                ProfileFragment profileFragment = new ProfileFragment();

                fragmentManager.beginTransaction()
                        .add(R.id.fragment_frame, profileFragment)
                        .commit();

                mBottomNavigation.setCurrentItem(PROFILE_FRAGMENT);
            }
            else
            {
                HomeFragment homeFragment = new HomeFragment();

                fragmentManager.beginTransaction()
                        .add(R.id.fragment_frame, homeFragment)
                        .commit();
            }
        }
    }

}
