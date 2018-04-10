package com.example.android.lab1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class MainActivity extends AppCompatActivity {

    ImageView mToolbarIcon;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextInputLayout mAddressTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;
    ImageView mCircleImageView;
    private Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int RC_SIGN_IN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    openMainActivity();
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setTheme(R.style.LoginTheme)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.FacebookBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                openMainActivity();
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this,"VINCENZO IS OFFLINE", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void openMainActivity(){
        setContentView(R.layout.activity_main);

        mUsernameTextInputLayout = findViewById(R.id.username_text);

        mPhoneTextInputLayout = findViewById(R.id.phone_text);

        mEmailTextInputLayout = findViewById(R.id.email_text);

        mAddressTextInputLayout = findViewById(R.id.address_text_view);

        mCircleImageView = findViewById(R.id.profile_image);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        mToolbarIcon = findViewById(R.id.icon_toolbar);

        mToolbar = findViewById(R.id.toolbar_main_activity);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        final SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();

        if (mUsernameTextInputLayout.getEditText() != null) {
            if (user != null)
                mUsernameTextInputLayout.getEditText().setText(user.getUsername());
            mUsernameTextInputLayout.getEditText().setKeyListener(null);
            mUsernameTextInputLayout.getEditText().setEnabled(false);
        }
        if (mEmailTextInputLayout.getEditText() != null) {
            if (user != null)
                mEmailTextInputLayout.getEditText().setText(user.getEmail());
            mEmailTextInputLayout.getEditText().setKeyListener(null);
            mEmailTextInputLayout.getEditText().setEnabled(false);
        }
        if (mPhoneTextInputLayout.getEditText() != null) {
            if (user != null)
                mPhoneTextInputLayout.getEditText().setText(user.getPhone());
            mPhoneTextInputLayout.getEditText().setKeyListener(null);
            mPhoneTextInputLayout.getEditText().setEnabled(false);
        }

        if (mShortBioTextInputLayout.getEditText() != null) {
            if (user != null)
                mShortBioTextInputLayout.getEditText().setText(user.getShortBio());
            mShortBioTextInputLayout.getEditText().setKeyListener(null);
            mShortBioTextInputLayout.getEditText().setEnabled(false);
        }

        if (user != null && user.getImage() != null) {
            Glide.with(this).load(user.getImage())
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        } else {
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        }

        if (mAddressTextInputLayout.getEditText() != null) {
            if (user != null)
                mAddressTextInputLayout.getEditText().setText(user.getAddress());
            mAddressTextInputLayout.getEditText().setKeyListener(null);
            mAddressTextInputLayout.getEditText().setEnabled(false);
        }

        mToolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                User u = sharedPreferencesManager.getUser();
                if (u != null)
                    intent.putExtra(ADDRESS_KEY, u.getAddress());
                startActivity(intent);
            }
        });


    }
}
