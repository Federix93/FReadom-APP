package com.example.android.lab1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.Spinner;
import android.widget.TextView;

public class LoadBookActivity extends Activity
{
    private ImageSwitcher mGallery;
    private EditText      mIsbnEditText;
    private EditText      mTitleEditText;
    private EditText      mAuthorEditText;
    private EditText      mPublisherEditText;
    private TextView      mPublishYearTextView;
    private Spinner       mPublishYearSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
}
