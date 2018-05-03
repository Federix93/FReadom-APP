package com.example.android.lab1.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.android.lab1.R;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.utils.Utilities;

import java.util.Calendar;

import fr.ganfra.materialspinner.MaterialSpinner;

public class LoadBookActivity2 extends AppCompatActivity {

    private Toolbar mToolbar;
    private MaterialSpinner mPublishYearSpinner;
    private MaterialSpinner mConditionsSpinner;
    private MaterialSpinner mGenreSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book_2);

        setupToolBar();
        setupSpinners();
    }

    private void setupToolBar() {
        // toolbar
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.load_book_toolbar_title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mToolbar.inflateMenu(R.menu.add_book);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /*
                int clickedItem = item.getItemId();
                // preventing double, using threshold of 1000 ms
                lastClickTime = SystemClock.elapsedRealtime();
                if (clickedItem == R.id.action_confirm) {
                    if (checkObligatoryFields()) {
                        mToolbar.setOnMenuItemClickListener(null);

                        lockUI(true);
                        mUploading = true;
                        mDocumentUploaded = false;
                        mIsbnImageView.setVisibility(View.GONE);
                        final ProgressBar mProgressBar = findViewById(R.id.load_book_progress_bar);
                        mProgressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),
                                R.string.load_book_upload,
                                Toast.LENGTH_SHORT)
                                .show();

                        if (mPhotosPath != null && mPhotosPath.size() > 0)
                            uploadPhotos();
                        else {
                            mImagesUploaded = true;
                            uploadBookInfo();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.load_book_fill_fields,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }*/
                return true;

            }
        });
        setSupportActionBar(mToolbar);
    }

    private void setupSpinners() {
        mPublishYearSpinner = findViewById(R.id.load_book_publish_year_spinner);
        mConditionsSpinner = findViewById(R.id.load_book_conditions_spinner);
        mGenreSpinner = findViewById(R.id.load_book_genre);

        String years[] = new String[200];

        for (int i = Calendar.getInstance().get(Calendar.YEAR), n = 0; n < 200; i--, n++)
            years[n] = Integer.toString(i);

        mPublishYearSpinner.setAdapter(Utilities.makeDropDownAdapter(this, years));
        mPublishYearSpinner.setHint(R.string.publishing_year);
        mPublishYearSpinner.setFloatingLabelText(R.string.publishing_year);

        mConditionsSpinner.setHint(R.string.conditions);
        mConditionsSpinner.setAdapter(Utilities.makeDropDownAdapter(this, Condition.getConditions(this).toArray(new String[0])));
        mConditionsSpinner.setFloatingLabelText(R.string.conditions);

        mGenreSpinner.setHint(R.string.genre_hint);
        mGenreSpinner.setAdapter(Utilities.makeDropDownAdapter(this,
                getResources().getStringArray(R.array.genre)));
        mGenreSpinner.setFloatingLabelText(R.string.genre_hint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSnackbar(R.string.load_book_upload_error);
    }

    private void showSnackbar(int messageResourceId) {
        Snackbar.make(findViewById(R.id.load_book_coordinator_root),
                messageResourceId,
                Snackbar.LENGTH_SHORT)
                .show();
    }
}
