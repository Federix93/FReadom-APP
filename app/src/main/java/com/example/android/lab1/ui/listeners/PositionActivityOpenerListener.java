package com.example.android.lab1.ui.listeners;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.android.lab1.R;
import com.example.android.lab1.ui.PositionActivity;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;

public class PositionActivityOpenerListener implements View.OnClickListener {

    private Activity mContainerActivity;
    private boolean mSearchCities;

    public PositionActivityOpenerListener(Activity mContainerActivity, boolean searchCities) {
        this.mContainerActivity = mContainerActivity;
        this.mSearchCities = searchCities;
    }

    // classe temporanea da usare come listener per il tasto che apre la position activity nella home page

    @Override
    public void onClick(View v) {
        final Intent i = Utilities.getPositionActivityIntent(mContainerActivity, mSearchCities);
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle(R.string.search_by_address_or_current_position)
                .setItems(R.array.search_by_address_or_current_position_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                i.putExtra(PositionActivity.START_SEARCH, true);
                            default:
                                if (Utilities.checkPermissionActivity(mContainerActivity, android.Manifest.permission.ACCESS_FINE_LOCATION))
                                    mContainerActivity.startActivityForResult(i, Constants.POSITION_ACTIVITY_REQUEST);
                                else
                                    Utilities.askPermissionActivity(mContainerActivity, android.Manifest.permission.ACCESS_FINE_LOCATION, Constants.POSITION_ACTIVITY_REQUEST);

                        }
                    }
                }).show();
    }
}
