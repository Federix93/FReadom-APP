package com.example.android.lab1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;

public class NetworkConnectionReceiver extends BroadcastReceiver {

    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    private AlertDialog.Builder mAlertDialog;

    public NetworkConnectionReceiver(){
        super();
    }

    public NetworkConnectionReceiver(Activity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        NetworkInfo activeNetwork = null;
        if(mProgressDialog == null){
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(mActivity.getString(R.string.connection_internet_dialog));
            mProgressDialog.setCancelable(false);
        }
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null)
             activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            mFirebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            if (user != null) {
                for (UserInfo userInfo : mFirebaseAuth.getCurrentUser().getProviderData()) {
                    if (userInfo.getProviderId().equals("password")) {
                        if(!user.isEmailVerified()){
                            mAlertDialog = new AlertDialog.Builder(mActivity);
                            mAlertDialog.setMessage(mActivity.getString(R.string.email_verification));
                            mAlertDialog.setCancelable(false);
                            mAlertDialog.setPositiveButton(mActivity.getString(R.string.verification_email_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    mActivity.startActivityForResult(
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
                                            10);
                                }
                            });
                            mAlertDialog.show();
                        }
                    }
                }
            }
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }else {
            mProgressDialog.show();
        }
    }
}
