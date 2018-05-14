package com.example.android.lab1.ui.searchbooks;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lab1.R;

public class FiltersFragment extends DialogFragment {

    private CheckBox mTitleCheckbox;
    private CheckBox mAuthorCheckbox;
    private CheckBox mPublisherCheckbox;
    private CheckBox mTagsCheckbox;
    private SeekBar mUserRatingSeekBar;
    private TextView mUserRatingTextView;
    private SeekBar mPositionSeekBar;
    private TextView mPositionTextView;

    public FiltersFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static FiltersFragment newInstance(String title) {
        FiltersFragment frag = new FiltersFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_filter_dialog_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
//        check1 = view.findViewById(R.id.checkbox_title);
//        check2 = view.findViewById(R.id.checkbox_author);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle("johnny");
    }

}
