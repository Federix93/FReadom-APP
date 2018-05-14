package com.example.android.lab1.ui.searchbooks;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
    private Button mPublishDateButton;
    private Button mRatingButton;
    private Button mConditionsButton;
    private Button mPositionButton;
    private Button mApplyButton;
    private Button mUndoButton;
    private Button mResetButton;

    boolean[] searchByFilters;
    int[] seekBarsFilters;
    boolean[] orderFilters;

    public FiltersFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static FiltersFragment newInstance(Bundle data) {
        FiltersFragment frag = new FiltersFragment();
        Bundle args = new Bundle();
        args.putBundle("filter_data", data);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_filter_dialog_fragment, container);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleCheckbox = view.findViewById(R.id.checkbox_title);
        mAuthorCheckbox = view.findViewById(R.id.checkbox_author);
        mPublisherCheckbox = view.findViewById(R.id.checkbox_publisher);
        mTagsCheckbox = view.findViewById(R.id.checkbox_tags);
        mUserRatingSeekBar = view.findViewById(R.id.user_rating_seek_bar);
        mUserRatingTextView = view.findViewById(R.id.selected_rating_text_view);
        mPositionSeekBar = view.findViewById(R.id.position_seek_bar);
        mPositionTextView = view.findViewById(R.id.selected_position_text_view);
        mPublishDateButton = view.findViewById(R.id.button_publish_date);
        mRatingButton = view.findViewById(R.id.button_user_rating);
        mConditionsButton = view.findViewById(R.id.button_condition);
        mPositionButton = view.findViewById(R.id.button_position);
        mApplyButton = view.findViewById(R.id.apply_filter_button);
        mUndoButton = view.findViewById(R.id.undo_filter_button);
        mResetButton = view.findViewById(R.id.reset_filters_button);

        Bundle data = getArguments().getBundle("filter_data");

        searchByFilters = data.getBooleanArray("search_by").clone();
        seekBarsFilters = data.getIntArray("seek_bars").clone();
        orderFilters = data.getBooleanArray("order_filters").clone();

        mTitleCheckbox.setChecked(searchByFilters[0]);
        mAuthorCheckbox.setChecked(searchByFilters[1]);
        mPublisherCheckbox.setChecked(searchByFilters[2]);
        mTagsCheckbox.setChecked(searchByFilters[3]);

        mUserRatingSeekBar.setProgress(seekBarsFilters[0]);
        mPositionSeekBar.setProgress(seekBarsFilters[1]);

        if(orderFilters[0])
            mPublishDateButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryAccent)));
        else if(orderFilters[1])
            mRatingButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryAccent)));
        else if(orderFilters[2])
            mConditionsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryAccent)));
        else if(orderFilters[3])
            mPositionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryAccent)));


    }

}
