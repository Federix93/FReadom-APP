package com.example.android.lab1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;

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
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int RC_SIGN_IN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                setLoggedUser(user);
                openMainActivity();
            }
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setTheme(R.style.LoginTheme)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.FacebookBuilder().build(),
                                    new AuthUI.IdpConfig.TwitterBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        if (mFirebaseAuth.getCurrentUser() != null) {
            mFirebaseAuth.getCurrentUser().reload();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    for (UserInfo userInfo : user.getProviderData()) {
                        if (userInfo.getProviderId().equals("password")) {
                            if (!user.isEmailVerified()) {
                                user.sendEmailVerification();
                                if (!mProgressDialog.isShowing()) {
                                    mProgressDialog.setMessage("Verifica dell'email");
                                    mProgressDialog.setCancelable(false);
                                    mProgressDialog.show();
                                }
                            }
                            break;
                        }
                    }
                    setLoggedUser(user);
                    openMainActivity();
                }
            } else {
                if (response == null) {
                    Toast.makeText(MainActivity.this, "YOU PRESSED BACK BUTTON", Toast.LENGTH_LONG).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    private void openMainActivity() {
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

    private void setLoggedUser(FirebaseUser user) {
        final SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        User userLocal = sharedPreferencesManager.getUser();
        if (userLocal == null)
            userLocal = User.getInstance();
        userLocal.setEmail(user.getEmail());
        if (user.getPhotoUrl() != null)
            userLocal.setImage(user.getPhotoUrl().toString());
        userLocal.setUsername(user.getDisplayName());
        sharedPreferencesManager.putUser(userLocal);
    }
}
