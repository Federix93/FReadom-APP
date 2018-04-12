package com.example.android.lab1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;

public class NetworkConnectionReceiver extends BroadcastReceiver {

    private Activity mActivity;
    private ProgressDialog mProgressDialog;

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
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
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

        }else {
            mProgressDialog.show();
        }
    }
}
