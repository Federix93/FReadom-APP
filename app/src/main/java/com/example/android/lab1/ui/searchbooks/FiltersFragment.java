package com.example.android.lab1.ui.searchbooks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
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
    private AppCompatButton mApplyButton;
    private AppCompatButton mUndoButton;
    private AppCompatButton mResetButton;

    FilterDataPass dataPasser;
    boolean[] searchByFilters;
    int[] seekBarsFilters;

    public FiltersFragment() {
    }

    public static FiltersFragment newInstance(Bundle data) {
        FiltersFragment frag = new FiltersFragment();
        Bundle args = new Bundle();
        args.putBundle(FiltersValues.FILTER_BUNDLE, data);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (FilterDataPass) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_filter_dialog_fragment, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

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
        mApplyButton = view.findViewById(R.id.apply_filter_button);
        mUndoButton = view.findViewById(R.id.undo_filter_button);
        mResetButton = view.findViewById(R.id.reset_filters_button);

        Bundle data = getArguments().getBundle(FiltersValues.FILTER_BUNDLE);

        searchByFilters = data.getBooleanArray(FiltersValues.SEARCH_BY).clone();
        seekBarsFilters = data.getIntArray(FiltersValues.SEEK_BARS).clone();

        setupSearchByListeners();
        setupSeekBarsListeners();
        setFilters();

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putBooleanArray(FiltersValues.SEARCH_BY, searchByFilters);
                b.putIntArray(FiltersValues.SEEK_BARS, seekBarsFilters);
                dataPasser.filterDataPass(b);
                dismiss();
            }
        });

        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchByFilters[FiltersValues.SEARCH_BY_TITLE] = true;
                searchByFilters[FiltersValues.SEARCH_BY_AUTHOR] = true;
                searchByFilters[FiltersValues.SEARCH_BY_PUBLISHER] = true;
                searchByFilters[FiltersValues.SEARCH_BY_TAGS] = true;
                seekBarsFilters[FiltersValues.RATING_FILTER] = 0;
                seekBarsFilters[FiltersValues.POSITION_FILTER] = FiltersValues.MAX_POSITION;
                setFilters();
            }
        });


    }

    private void setupSearchByListeners() {

        mTitleCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mTitleCheckbox.isChecked())
                {
                    if(mAuthorCheckbox.isChecked() || mPublisherCheckbox.isChecked() || mTagsCheckbox.isChecked())
                    {
                        searchByFilters[FiltersValues.SEARCH_BY_TITLE] = false;
                    }
                    else
                    {
                        mTitleCheckbox.setChecked(true);
                        Toast.makeText(getContext(), getResources().getString(R.string.min_selection), Toast.LENGTH_SHORT).show();
                    }

                }
                else
                    searchByFilters[FiltersValues.SEARCH_BY_TITLE] = true;


            }
        });

        mAuthorCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mAuthorCheckbox.isChecked())
                {
                    if(mTitleCheckbox.isChecked() || mPublisherCheckbox.isChecked() || mTagsCheckbox.isChecked())
                    {
                        searchByFilters[FiltersValues.SEARCH_BY_AUTHOR] = false;
                    }
                    else
                    {
                        mAuthorCheckbox.setChecked(true);
                        Toast.makeText(getContext(), getResources().getString(R.string.min_selection), Toast.LENGTH_SHORT).show();
                    }

                }
                else
                    searchByFilters[FiltersValues.SEARCH_BY_AUTHOR] = true;

            }
        });

        mPublisherCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mPublisherCheckbox.isChecked())
                {
                    if(mAuthorCheckbox.isChecked() || mTitleCheckbox.isChecked() || mTagsCheckbox.isChecked())
                    {
                        searchByFilters[FiltersValues.SEARCH_BY_PUBLISHER] = false;
                    }
                    else
                    {
                        mPublisherCheckbox.setChecked(true);
                        Toast.makeText(getContext(), getResources().getString(R.string.min_selection), Toast.LENGTH_SHORT).show();
                    }

                }
                else
                    searchByFilters[FiltersValues.SEARCH_BY_PUBLISHER] = true;

            }
        });

        mTagsCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mTagsCheckbox.isChecked())
                {
                    if(mAuthorCheckbox.isChecked() || mPublisherCheckbox.isChecked() || mTitleCheckbox.isChecked())
                    {
                        searchByFilters[FiltersValues.SEARCH_BY_TAGS] = false;
                    }
                    else
                    {
                        mTagsCheckbox.setChecked(true);
                        Toast.makeText(getContext(), getResources().getString(R.string.min_selection), Toast.LENGTH_SHORT).show();
                    }

                }
                else
                    searchByFilters[FiltersValues.SEARCH_BY_TAGS] = true;

            }
        });

    }

    private void setFilters() {

        mTitleCheckbox.setChecked(searchByFilters[FiltersValues.SEARCH_BY_TITLE]);
        mAuthorCheckbox.setChecked(searchByFilters[FiltersValues.SEARCH_BY_AUTHOR]);
        mPublisherCheckbox.setChecked(searchByFilters[FiltersValues.SEARCH_BY_PUBLISHER]);
        mTagsCheckbox.setChecked(searchByFilters[FiltersValues.SEARCH_BY_TAGS]);

        mUserRatingSeekBar.setProgress(seekBarsFilters[FiltersValues.RATING_FILTER]-10);
        setRatingFilterText(seekBarsFilters[FiltersValues.RATING_FILTER]);
        mPositionSeekBar.setProgress(seekBarsFilters[FiltersValues.POSITION_FILTER]);
        setPositionFilterText(seekBarsFilters[FiltersValues.POSITION_FILTER]);
    }

    private void setupSeekBarsListeners() {

        mUserRatingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setRatingFilterText(progress+10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarsFilters[FiltersValues.RATING_FILTER] = seekBar.getProgress()+10;
            }
        });

        mPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setPositionFilterText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarsFilters[FiltersValues.POSITION_FILTER] = seekBar.getProgress();
            }
        });

    }

    private void setRatingFilterText(int value)
    {
        if(value <= 10 )
            mUserRatingTextView.setText(getResources().getString(R.string.rating_filter_zero));
        else if(value == 50)
            mUserRatingTextView.setText(getResources().getString(R.string.rating_filter_max));
        else
            mUserRatingTextView.setText(String.format(getResources().getString(R.string.rating_filter), ((float)value/10)));
    }

    private void setPositionFilterText(int value)
    {
        mPositionTextView.setText(String.format(getResources().getString(R.string.position_filter), ((float)(value+10)/10)));
    }


}
