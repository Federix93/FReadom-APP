package com.example.android.lab1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkReceiver extends BroadcastReceiver {

    public static final String NETWORK_AVAILABILITY_RESULT = "NETWORK_AVAILABLE";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        boolean connected = false;
        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED)
            connected = true;
        else if (intent != null && intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
            connected = false;

        Intent result = new Intent(PositionActivity.INTERNET_AVAILABLE_BROAD_KEY);
        result.putExtra(NETWORK_AVAILABILITY_RESULT, connected);
        context.sendBroadcast(result);
    }

}