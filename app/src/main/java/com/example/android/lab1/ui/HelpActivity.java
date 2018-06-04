package com.example.android.lab1.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.utils.Utilities;

public class HelpActivity extends FragmentActivity {

    Toolbar mToolbar;
    TextView mTermsAndConditionsTextView;
    TextView mLicenseTextView;
    Fragment fragment = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mToolbar = findViewById(R.id.toolbar_help_activity);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Utilities.setupStatusBarColor(this);

        mTermsAndConditionsTextView = findViewById(R.id.terms_textView);
        mLicenseTextView = findViewById(R.id.license_textView);
        updateUI();
        mTermsAndConditionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTermsAndConditionsTextView.setVisibility(View.GONE);
                mLicenseTextView.setVisibility(View.GONE);
                mToolbar.setTitle(R.string.terms_and_conditions);
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                fragment = InformationFragment.newInstance("TERMS");
                transaction.add(R.id.container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        mLicenseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTermsAndConditionsTextView.setVisibility(View.GONE);
                mLicenseTextView.setVisibility(View.GONE);
                mToolbar.setTitle(R.string.license_open_source);
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                fragment = InformationFragment.newInstance("LICENSE");
                transaction.add(R.id.container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void updateUI() {
        mToolbar.setTitle(R.string.informations);
        if(mTermsAndConditionsTextView.getVisibility() == View.GONE)
            mTermsAndConditionsTextView.setVisibility(View.VISIBLE);
        if (mLicenseTextView.getVisibility() == View.GONE)
            mLicenseTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {

       if(fragment != null){
           updateUI();
           FragmentTransaction ft = getFragmentManager().beginTransaction();
           ft.remove(fragment);
           ft.commit();
           getFragmentManager().popBackStack();
           fragment = null;
       }else{
           super.onBackPressed();
       }
    }
}
