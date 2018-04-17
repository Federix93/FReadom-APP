package com.example.android.lab1;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    private FirebaseAuth mFirebaseAuth;

    private NetworkConnectionReceiver mNetworkConnectionBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mNetworkConnectionBroadcastReceiver = new NetworkConnectionReceiver(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            if (!isEmailAndPasswordProvider()) {
                FirebaseManager.addUser(user);
                openHomePageActivity();
            } else {
                user.reload();
                if (user.isEmailVerified()) {
                    FirebaseManager.addUser(user);
                    openHomePageActivity();
                }
            }
        } else {
            openFirebaseUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkConnectionBroadcastReceiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkConnectionBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    boolean isPasswordMode = false;
                    for (UserInfo userInfo : user.getProviderData()) {
                        if (userInfo.getProviderId().equals("password")) {
                            if (!user.isEmailVerified()) {
                                isPasswordMode = true;
                                user.sendEmailVerification();
                            }
                            break;
                        }
                    }
                    if(!isPasswordMode) {
                        FirebaseManager.addUser(user);
                        openHomePageActivity();
                    }
                }
            } else {
                if (response == null) {
                    finish();
                    return;
                }
                /*if (response.getError() != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {

                }*/
            }
        }
    }

    private boolean isEmailAndPasswordProvider() {
        for (UserInfo userInfo : mFirebaseAuth.getCurrentUser().getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                return true;
            }
        }
        return false;
    }

    private void openHomePageActivity(){
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    private void openFirebaseUI(){
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
