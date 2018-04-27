package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.example.android.lab1.R;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager mFragmentManager;
    HomeFragment mHomeFragment;
    DashboardFragment mDashboardFragment;
    ProfileFragment mProfileFragment;
    AHBottomNavigation mBottomNavigation;

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

        if(savedInstanceState == null) {

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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
