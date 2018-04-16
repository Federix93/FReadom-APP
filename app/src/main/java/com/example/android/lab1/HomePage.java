package com.example.android.lab1;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity {

    private static final int HOME_FRAGMENT = 0;
    private static final int DASH_FRAGMENT = 1;
    private static final int PROFILE_FRAGMENT = 2;

    private AHBottomNavigation mBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

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
                            ft.commit();
                            Toast.makeText(HomePage.this, "Function not implemented", Toast.LENGTH_SHORT).show();
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
/*
            HomeFragment homeFragment = new HomeFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_frame, homeFragment)
                    .commit();
                    */

            //trovare un modo migliore per passare un messaggio quando il profilo viene salvato perchè
            //potrebbe dare problemi quando viene utilizzata onSavedInstanceState in questa classe

            //Tutto ciò viene fatto per far uscire come current profile il profilo e non l'homepage
            if (getIntent().getExtras() != null)
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
