package com.example.android.lab1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
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
                    Toast.makeText(SignInActivity.this, "YOU PRESSED BACK BUTTON", Toast.LENGTH_LONG).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
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
}
