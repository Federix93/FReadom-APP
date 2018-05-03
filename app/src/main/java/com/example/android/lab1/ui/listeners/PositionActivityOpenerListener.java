package com.example.android.lab1.ui.listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;

public class PositionActivityOpenerListener implements View.OnClickListener {

    private Activity mContainerActivity;

    public PositionActivityOpenerListener(Activity mContainerActivity) {
        this.mContainerActivity = mContainerActivity;
    }

    // classe temporanea da usare come listener per il tasto che apre la position activity nella home page

    @Override
    public void onClick(View v) {
        Intent i = Utilities.getPositionActivityIntent(mContainerActivity, true);
        mContainerActivity.startActivityForResult(i, Constants.POSITION_ACTIVITY_REQUEST);
    }
}
