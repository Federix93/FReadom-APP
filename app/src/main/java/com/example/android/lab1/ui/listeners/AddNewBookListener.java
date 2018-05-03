package com.example.android.lab1.ui.listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.android.lab1.ui.LoadBookActivity;
import com.example.android.lab1.utils.Constants;

public class AddNewBookListener implements View.OnClickListener {
    private Activity mActivity;
    private String mInitialIsbn;

    public AddNewBookListener(Activity mActivity, String mInitialIsbn) {
        this.mActivity = mActivity;
        this.mInitialIsbn = mInitialIsbn;
    }

    public AddNewBookListener(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void onClick(View v) {
        //new IsbnDialog().show(mActivity.getFragmentManager(), "ISBN_DIALOG");
        Intent i = new Intent(mActivity, LoadBookActivity.class);
        mActivity.startActivityForResult(i, Constants.LOAD_BOOK_REQUEST);
    }
}
