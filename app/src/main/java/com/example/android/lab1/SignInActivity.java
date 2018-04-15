package com.example.android.lab1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;

    private NetworkConnectionReceiver mNetworkConnectionBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mNetworkConnectionBroadcastReceiver = new NetworkConnectionReceiver(this);

        mProgressDialog = new ProgressDialog(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            if (!isEmailAndPasswordProvider()) {
                setLoggedUser(user);
                openMainActivity();
            } else {
                user.reload();
                if (!user.isEmailVerified()) {
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.setMessage("Verifica dell'email");
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                    }
                }else{
                    setLoggedUser(user);
                    openMainActivity();
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
                                if (!mProgressDialog.isShowing()) {
                                    mProgressDialog.setMessage("Verifica dell'email");
                                    mProgressDialog.setCancelable(false);
                                    mProgressDialog.show();
                                }
                            }
                            break;
                        }
                    }
                    if(!isPasswordMode) {
                        setLoggedUser(user);
                        openMainActivity();
                    }
                }
            } else {
                if (response == null) {
                    finish();
                    return;
                }
                if (response.getError() != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {


                }
            }
        }
    }
    private void setLoggedUser(FirebaseUser user) {
        final SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        User userLocal = sharedPreferencesManager.getUser();
        if (userLocal == null) {
            userLocal = User.getInstance();
            userLocal.setEmail(user.getEmail());
            if (user.getPhotoUrl() != null)
                userLocal.setImage(user.getPhotoUrl().toString());
            userLocal.setUsername(user.getDisplayName());
            sharedPreferencesManager.putUser(userLocal);
        }
        FirebaseManager.addUser(user);

    }

    private boolean isEmailAndPasswordProvider() {
        for (UserInfo userInfo : mFirebaseAuth.getCurrentUser().getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                return true;
            }
        }
        return false;
    }

    private void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
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
