package com.example.android.lab1.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.AuthorAdapter;
import com.example.android.lab1.adapter.PhotosAdapter;
import com.example.android.lab1.adapter.RemovePhotoClickListener;
import com.example.android.lab1.model.Address;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.ui.listeners.PositionActivityOpenerListener;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.example.android.lab1.utils.Constants.POSITION_ACTIVITY_REQUEST;
import static com.example.android.lab1.utils.Constants.RESULT_LOAD_IMAGE;
import static com.example.android.lab1.utils.Constants.SCAN_REQUEST_TAG;

public class LoadBookActivity extends AppCompatActivity implements View.OnClickListener, RemovePhotoClickListener {

    /*Algolia connection variables*/
    Client algoliaClient;
    Index algoliaIndex;
    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_API_KEY = "36664d38d1ffa619b47a8b56069835d1";
    private final static String ALGOLIA_BOOK_INDEX = "books";

    private static final String ACTIVITY_STATE = "STATE";
    private static final String RESULT_BOOK = "RESULT";
    private static final String PHOTOS_KEY = "USER_PHOTOS";
    private static final String PUBLISH_YEAR_SPINNER = "PUBLISH_YEAR_SPINNER";
    private static final String CONDITION_SPINNER = "CONDITION_SPINNER";
    private static final String GENRE_SPINNER = "GENRE_SPINNER";
    private static final String ISBN = "ISBN";
    private static final String CURRENT_PHOTO = "CURRENT_PHOTO";
    private static final String KEY_RECYCLER_STATE = "KEY_RECYCLER";
    private static final String UPLOADING = "UPLOADING";
    private static final String AUTHORS = "AUTHORS";
    private static final String AUTHORS_EDITABLE = "A_EDITABLE";
    private Toolbar mToolbar;
    private LinearLayoutCompat mIsbnContainer;
    private MaterialSpinner mPublishYearSpinner;
    private MaterialSpinner mConditionsSpinner;
    private MaterialSpinner mGenreSpinner;
    private EditText mIsbnEditText;

    private NestedScrollView mInfoContainerScrollView;
    private ImageView mBookWebThumbnailImageView;
    private TextInputLayout mTitleTextInputLayout;
    private RecyclerView mAuthorsListView;
    private TextInputLayout mPublisherTextInputLayout;
    private AppCompatButton mPositionTextView;
    private AppCompatButton mLoanRangeTextView;
    private AppCompatButton mScanBarcodeButton;
    private AppCompatButton mInsertManuallyButton;
    private ImageView mIsbnExplanationImageView;
    private RequestQueue mRequestQueue;
    private ProgressBar mProgressBar;
    private RecyclerView mPhotosGrid;
    private ImageButton mAddPhotos;

    private String mCurrentTitle;
    private Book mResultBook;

    private AlertDialog.Builder mAlertDialogBuilder;
    private File mPhotoFile;
    private State mActivityState;
    private boolean mUploading;
    private AppCompatButton mConfirmButton;
    private int mProgressRate;
    private int mUploadedImagesCount;
    private ArrayList<Object> mDownloadUrls;
    private StorageReference mStorageRef;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book_2);
        initializeWidgets();

        mActivityState = State.ISBN_CHOICE;
        setActivityState();
        mUploading = false;
        Utilities.setupStatusBarColor(this);
        setupToolBar();
        setupSpinners();
        setupListeners();
        setupGallery(savedInstanceState != null && savedInstanceState.containsKey(PHOTOS_KEY) ?
                savedInstanceState.getStringArrayList(PHOTOS_KEY) : null);

        setupAlgolia();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivityState != null)
            outState.putString(ACTIVITY_STATE, mActivityState.toString());
        if (mActivityState.equals(State.ISBN_MANUALLY) && mIsbnEditText != null)
            outState.putString(ISBN, mIsbnEditText.getText().toString());

        if (mResultBook != null)
            outState.putParcelable(RESULT_BOOK, mResultBook);
        if (mPhotosGrid != null && mPhotosGrid.getAdapter() != null
                && mPhotosGrid.getAdapter().getItemCount() > 0) {
            // save photos
            if (mPhotosGrid.getAdapter() instanceof PhotosAdapter) {
                PhotosAdapter ph = (PhotosAdapter) mPhotosGrid.getAdapter();
                outState.putStringArrayList(PHOTOS_KEY, ph.getModel());
            }
        }
        if (mPublishYearSpinner.getSelectedItemPosition() > 0)
            outState.putInt(PUBLISH_YEAR_SPINNER, mPublishYearSpinner.getSelectedItemPosition());
        if (mConditionsSpinner.getSelectedItemPosition() > 0)
            outState.putInt(CONDITION_SPINNER, mConditionsSpinner.getSelectedItemPosition());
        if (mGenreSpinner.getSelectedItemPosition() > 0)
            outState.putInt(GENRE_SPINNER, mGenreSpinner.getSelectedItemPosition());
        if (mPhotoFile != null)
            outState.putString(CURRENT_PHOTO, mPhotoFile.getAbsolutePath());
        if (mAuthorsListView != null && mAuthorsListView.getAdapter() != null) {
            AuthorAdapter ph = (AuthorAdapter) mAuthorsListView.getAdapter();
            if (ph.getAuthors() != null) {
                outState.putStringArrayList(AUTHORS, ph.getAuthors());
                outState.putBoolean(AUTHORS_EDITABLE, ph.getEditable());
            }
        }

        outState.putBoolean(UPLOADING, mUploading);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUploading = savedInstanceState.getBoolean(UPLOADING);
        if (savedInstanceState.containsKey(ACTIVITY_STATE))
            mActivityState = State.valueOf(savedInstanceState.getString(ACTIVITY_STATE));
        if (savedInstanceState.containsKey(ISBN) && mIsbnEditText != null)
            mIsbnEditText.setText(savedInstanceState.getString(ISBN));
        if (savedInstanceState.containsKey(CURRENT_PHOTO)) {
            if (savedInstanceState.getString(CURRENT_PHOTO) != null)
                mPhotoFile = new File(savedInstanceState.getString(CURRENT_PHOTO));
        }
        if (savedInstanceState.containsKey(RESULT_BOOK)) {
            mResultBook = savedInstanceState.getParcelable(RESULT_BOOK);
            if (mResultBook != null && mResultBook.getWebThumbnail() != null && mBookWebThumbnailImageView != null)
                Glide.with(getApplicationContext())
                        .load(mResultBook.getWebThumbnail())
                        .into(mBookWebThumbnailImageView);

            if (mResultBook != null && mResultBook.getIsbn() != null && mIsbnEditText != null)
                mIsbnEditText.setText(mResultBook.getIsbn());
            if (mResultBook != null && mResultBook.getTitle() != null) {
                mCurrentTitle = mResultBook.getTitle();
                if (mTitleTextInputLayout != null && mTitleTextInputLayout.getEditText() != null) {
                    mTitleTextInputLayout.getEditText().setText(mCurrentTitle);
                    mTitleTextInputLayout.getEditText().setEnabled(false);
                }
            }
            if (mResultBook != null && mResultBook.getAuthors() != null) {
                String[] authors = mResultBook.getAuthors().split(",");
                ArrayList<String> authorsList = new ArrayList<>();
                Collections.addAll(authorsList, authors);
                mAuthorsListView.setAdapter(new AuthorAdapter(getLayoutInflater(), authorsList));
            }

            if (mResultBook != null && mResultBook.getPublisher() != null && mPublisherTextInputLayout != null &&
                    mPublisherTextInputLayout.getEditText() != null) {
                mPublisherTextInputLayout.getEditText().setText(mResultBook.getPublisher());
                mPublisherTextInputLayout.getEditText().setEnabled(false);
            }

            if (mResultBook != null && mResultBook.getPublishYear() != null && mPublishYearSpinner != null)
                mPublishYearSpinner.setEnabled(false);

            if (mResultBook != null && mResultBook.getAddress() != null && mPositionTextView != null)
                mPositionTextView.setText(mResultBook.getAddress());
        }

        if (savedInstanceState.containsKey(PUBLISH_YEAR_SPINNER) && mPublishYearSpinner != null)
            mPublishYearSpinner.setSelection(savedInstanceState.getInt(PUBLISH_YEAR_SPINNER));
        if (savedInstanceState.containsKey(CONDITION_SPINNER) && mConditionsSpinner != null)
            mConditionsSpinner.setSelection(savedInstanceState.getInt(CONDITION_SPINNER));
        if (savedInstanceState.containsKey(GENRE_SPINNER) && mGenreSpinner != null)
            mGenreSpinner.setSelection(savedInstanceState.getInt(GENRE_SPINNER));

        if (savedInstanceState.containsKey(AUTHORS) && savedInstanceState.containsKey(AUTHORS_EDITABLE)) {
            AuthorAdapter authorAdapter = new AuthorAdapter(getLayoutInflater(),
                    savedInstanceState.getStringArrayList(AUTHORS));
            authorAdapter.setEditable(savedInstanceState.getBoolean(AUTHORS_EDITABLE));
            mAuthorsListView.setAdapter(authorAdapter);
        }

        setActivityState();
    }

    private void setActivityState() {
        switch (mActivityState) {
            case ISBN_MANUALLY:
                mCurrentTitle = getString(R.string.insert_isbn_code);
                mInsertManuallyButton.setText(R.string.confirm);
                mIsbnEditText.setEnabled(true);
                mInsertManuallyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsbnEditText != null &&
                                mIsbnEditText.getText() != null &&
                                mIsbnEditText.getText().length() > 0) {
                            // remove focus
                            mTitleTextInputLayout.getEditText().clearFocus();
                            mToolbar.requestFocus();
                            mPublishYearSpinner.clearFocus();
                            mPublishYearSpinner.clearFocus();
                            // make google api request
                            makeRequestBookApi(mIsbnEditText.getText().toString(), false);
                        }
                    }
                });
                mIsbnContainer.setVisibility(View.VISIBLE);
                mIsbnEditText.setText(null);
                TextInputLayout isbnContainer = findViewById(R.id.load_book_isbn);
                isbnContainer.setVisibility(View.VISIBLE);
                mIsbnEditText.requestFocus();
                mIsbnEditText.setVisibility(View.VISIBLE);
                mScanBarcodeButton.setVisibility(View.GONE);
                mIsbnExplanationImageView.setVisibility(View.VISIBLE);
                mInfoContainerScrollView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                break;
            case ISBN_ACQUIRED:
                mProgressBar.setVisibility(View.GONE);
                mIsbnContainer.setVisibility(View.GONE);
                mIsbnEditText.setEnabled(false);
                mIsbnExplanationImageView.setVisibility(View.GONE);
                mInfoContainerScrollView.setVisibility(View.VISIBLE);
                break;
            default:
                mProgressBar.setVisibility(View.GONE);
                mCurrentTitle = getString(R.string.insert_isbn_code);
                mInsertManuallyButton.setText(R.string.insert_manually);
                mInsertManuallyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivityState = State.ISBN_MANUALLY;
                        setActivityState();
                    }
                });
                mIsbnContainer.setVisibility(View.VISIBLE);
                mScanBarcodeButton.setVisibility(View.VISIBLE);
                findViewById(R.id.load_book_isbn).setVisibility(View.GONE);
                mIsbnExplanationImageView.setVisibility(View.VISIBLE);
                mInfoContainerScrollView.setVisibility(View.GONE);
                mInfoContainerScrollView.setScrollY(0);
                mConditionsSpinner.setSelection(0);
                mGenreSpinner.setSelection(0);
                mPositionTextView.setText(null);
                mLoanRangeTextView.setText(null);
                cleanPhotos();
                break;
        }

        mToolbar.setTitle(mCurrentTitle);
    }

    private void setupGallery(ArrayList<String> initialModel) {
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(this);
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPhotosGrid.setLayoutManager(MyLayoutManager);
        mPhotosGrid.setAdapter(initialModel == null ? new PhotosAdapter(this) : new PhotosAdapter(this, initialModel));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.SCAN_REQUEST_TAG:
                if (resultCode == Activity.RESULT_OK) {
                    // Parsing bar code reader mResultBook
                    if (data != null && data.hasExtra(ScanBarCodeActivity.BARCODE_KEY)) {
                        makeRequestBookApi(data.getStringExtra(ScanBarCodeActivity.BARCODE_KEY),
                                false);
                    }
                }
                break;
            case Constants.CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK && mPhotoFile != null) {
                    ((PhotosAdapter) mPhotosGrid.getAdapter()).addItem(mPhotoFile.getAbsolutePath());
                } else if (resultCode == RESULT_CANCELED && mPhotoFile != null) {
                    if (mPhotoFile.isFile())
                        mPhotoFile.delete();
                }
                break;
            case Constants.RESULT_LOAD_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.getData() != null) {
                        PhotosAdapter adapter = (PhotosAdapter) mPhotosGrid.getAdapter();
                        adapter.addItem(data.getData().toString());
                    }
                }
                break;
            case Constants.POSITION_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    Address address = Utilities.readResultOfPositionActivity(data);
                    mResultBook.setAddress(address.getAddress());
                    mResultBook.setGeoPoint(address.getLat(), address.getLon());
                    mPositionTextView.setText(address.getAddress());
                }
                break;
            case Constants.CALENDAR_REQUEST:
                if (resultCode == RESULT_OK) {
                    Calendar first, last;

                    first = data.hasExtra(CalendarActivity.FIRST_DATE) ?
                            (Calendar) data.getSerializableExtra(CalendarActivity.FIRST_DATE) : null;
                    last = data.hasExtra(CalendarActivity.LAST_DATE) ?
                            (Calendar) data.getSerializableExtra(CalendarActivity.LAST_DATE) : null;
                    if (mResultBook != null) {
                        if (first != null)
                            mResultBook.setLoanStart(first.getTimeInMillis());
                        else
                            mResultBook.setLoanStart(null);
                        if (last != null)
                            mResultBook.setLoanEnd(last.getTimeInMillis());
                        else
                            mResultBook.setLoanEnd(null);

                        DateFormat dmy = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                        if (first != null) {
                            if (last != null)
                                mLoanRangeTextView.setText(dmy.format(first.getTime()) + "-" + dmy.format(last.getTime()));
                            else
                                mLoanRangeTextView.setText(dmy.format(first.getTime()));
                        }

                    }
                }
                break;
        }
    }

    private void setupListeners() {

        mLoanRangeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoadBookActivity.this, CalendarActivity.class);
                startActivityForResult(intent, Constants.CALENDAR_REQUEST);
            }
        });

        mPositionTextView.setOnClickListener(new PositionActivityOpenerListener(this,
                false));

        mScanBarcodeButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      if (Utilities.checkPermissionActivity(LoadBookActivity.this, Manifest.permission.CAMERA)) {
                                                          Utilities.startBarcodeScanner(LoadBookActivity.this);
                                                      } else {
                                                          ActivityCompat.requestPermissions(LoadBookActivity.this,
                                                                  new String[]{Manifest.permission.CAMERA},
                                                                  SCAN_REQUEST_TAG);
                                                      }
                                                  }
                                              }
        );

        mAddPhotos.setOnClickListener(this);

        mIsbnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        if (mIsbnEditText != null &&
                                mIsbnEditText.getText() != null &&
                                mIsbnEditText.getText().length() > 0) {
                            // remove focus
                            mTitleTextInputLayout.getEditText().clearFocus();
                            mIsbnExplanationImageView.requestFocus();
                            mPublishYearSpinner.clearFocus();
                            mPublishYearSpinner.clearFocus();
                            // make google api request
                            makeRequestBookApi(mIsbnEditText.getText().toString(), false);
                        }
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                if (checkFields())
                    prepareAndStartUpload();
                else
                    v.setClickable(true);
            }
        });

    }

    private boolean checkFields() {
        // TODO
        return true;
    }

    private void initializeWidgets() {
        mToolbar = findViewById(R.id.toolbar_load_book);
        TextInputLayout isbnTextContainer = findViewById(R.id.load_book_isbn);
        mIsbnEditText = isbnTextContainer.getEditText();
        mIsbnContainer = findViewById(R.id.load_book_toolbar_isbn);
        mInsertManuallyButton = findViewById(R.id.load_book_search_manually);
        mIsbnExplanationImageView = findViewById(R.id.load_book_isbn_explanation);
        mInfoContainerScrollView = findViewById(R.id.load_book_info_container);
        mBookWebThumbnailImageView = findViewById(R.id.thumbnail);
        mTitleTextInputLayout = findViewById(R.id.load_book_title);
        mAuthorsListView = findViewById(R.id.load_book_author_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        mAuthorsListView.setHasFixedSize(true);
        mAuthorsListView.setLayoutManager(layoutManager);
        mPublisherTextInputLayout = findViewById(R.id.load_book_publisher);
        mPositionTextView = findViewById(R.id.load_book_position);
        mLoanRangeTextView = findViewById(R.id.load_book_loan_range);
        mScanBarcodeButton = findViewById(R.id.load_book_scan_bar_code);
        mPublishYearSpinner = findViewById(R.id.load_book_publish_year_spinner);
        mConditionsSpinner = findViewById(R.id.load_book_conditions_spinner);
        mGenreSpinner = findViewById(R.id.load_book_genre);
        mProgressBar = findViewById(R.id.load_book_progress_bar);
        mPhotosGrid = findViewById(R.id.load_book_loaded_photos);
        mAddPhotos = findViewById(R.id.load_book_add_photos);
        mConfirmButton = findViewById(R.id.load_book_confirm_book);
    }

    private void setupToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUploading) {
                    if (mActivityState.equals(State.ISBN_MANUALLY) || mActivityState.equals(State.ISBN_ACQUIRED)) {
                        if (mActivityState.equals(State.ISBN_ACQUIRED))
                            cleanPhotos();
                        mActivityState = State.ISBN_CHOICE;
                        setActivityState();
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!mUploading) {
            if (mActivityState.equals(State.ISBN_MANUALLY) || mActivityState.equals(State.ISBN_ACQUIRED)) {
                if (mActivityState.equals(State.ISBN_ACQUIRED)) {
                    cleanPhotos();
                }
                mActivityState = State.ISBN_CHOICE;
                setActivityState();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void cleanPhotos() {
        if (mPhotosGrid != null
                && mPhotosGrid.getAdapter() != null
                && mPhotosGrid.getAdapter().getItemCount() > 0) {
            for (int i = 0; i < mPhotosGrid.getAdapter().getItemCount(); i++) {
                this.removePhoto(i);
            }

        }
    }

    private void setupSpinners() {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SCAN_REQUEST_TAG:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Utilities.startBarcodeScanner(LoadBookActivity.this);
                }
                break;
            case POSITION_ACTIVITY_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Intent positionActivityIntent = Utilities.getPositionActivityIntent(LoadBookActivity.this, false);
                    startActivityForResult(positionActivityIntent, POSITION_ACTIVITY_REQUEST);
                }
                break;
            case Constants.CAPTURE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        if (mPhotoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    "com.example.android.fileprovider",
                                    mPhotoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        }
                        startActivityForResult(cameraIntent, Constants.CAPTURE_IMAGE);
                    }
                }
                break;

        }
    }

    private void makeRequestBookApi(final String isbn, final boolean alternativeEndPoint) {
        mInsertManuallyButton.setOnClickListener(null);
        final ProgressBar mProgressBar = findViewById(R.id.load_book_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);

        String url;
        String readIsbn = isbn.replace("-", "");
        if (readIsbn.length() == 10) {
            readIsbn = Utilities.Isbn10ToIsbn13(readIsbn);
        }
        if (!alternativeEndPoint)
            url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + readIsbn;
        else
            url = "https://www.googleapis.com/books/v1/volumes?q=ISBN:" + readIsbn;

        final String finalReadIsbn = readIsbn;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("totalItems") > 0) {
                        mResultBook = new Book();
                        JSONObject book = response.getJSONArray("items")
                                .getJSONObject(0).getJSONObject("volumeInfo");

                        mResultBook.setIsbn(finalReadIsbn);

                        mActivityState = State.ISBN_ACQUIRED;
                        mCurrentTitle = book.optString("title");
                        mResultBook.setTitle(mCurrentTitle);
                        mTitleTextInputLayout.getEditText().setText(mCurrentTitle);
                        if (mTitleTextInputLayout.getEditText().getText() != null &&
                                mTitleTextInputLayout.getEditText().getText().length() > 0)
                            mTitleTextInputLayout.getEditText().setEnabled(false);
                        /*if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0)
                            mAuthorEditText.getEditText().setText(book.getJSONArray("authors")
                                    .getString(0));
                        else
                            mAuthorEditText.getEditText().setText("");*/

                        mResultBook.setPublisher(book.optString("publisher"));
                        mPublisherTextInputLayout.getEditText().setText(mResultBook.getPublisher());
                        if (mResultBook.getPublisher() != null)
                            mPublisherTextInputLayout.getEditText().setEnabled(false);

                        if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0) {
                            JSONArray authors = book.getJSONArray("authors");
                            ArrayList<String> authorsList = new ArrayList<>();
                            for (int i = 0; i < authors.length(); i++) {
                                mResultBook.addAuthor(authors.getString(i));
                                authorsList.add(mResultBook.getAuthor(i));
                            }

                            AuthorAdapter authorAdapter = new AuthorAdapter(getLayoutInflater(),
                                    authorsList);
                            authorAdapter.setEditable(false);
                            mAuthorsListView.setAdapter(authorAdapter);
                            mAuthorsListView.getAdapter().notifyDataSetChanged();

                        } else {
                            AuthorAdapter authorAdapter = new AuthorAdapter(getLayoutInflater(), null);
                            authorAdapter.setEditable(true);
                            mAuthorsListView.setAdapter(authorAdapter);
                        }

                        if (book.has("publishedDate")) {
                            mResultBook.setPublishYear(book.getInt("publishedDate"));
                            int currentYear, publishYear;
                            currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            publishYear = mResultBook.getPublishYear();
                            mPublishYearSpinner.setSelection(currentYear - publishYear);
                        }


                        if (book.has("imageLinks")) {

                            JSONObject imageLinks = book.getJSONObject("imageLinks");
                            if (imageLinks.length() > 0 &&
                                    imageLinks.has("thumbnail")) {
                                mResultBook.setWebThumbnail(imageLinks.getString("thumbnail"));
                                Glide.with(getApplicationContext())
                                        .load(mResultBook.getWebThumbnail())
                                        .into(mBookWebThumbnailImageView);
                            }
                        }

                        if (book.has("infoLink"))
                            mResultBook.setInfoLink(book.getString("infoLink"));
                        else if (book.has("canonicalVolumeLink"))
                            mResultBook.setInfoLink(book.getString("canonicalVolumeLink"));

                        setActivityState();
                        mTitleTextInputLayout.getEditText().clearFocus();
                        mPublishYearSpinner.clearFocus();
                        mPublishYearSpinner.clearFocus();
                        mInfoContainerScrollView.requestFocus();

                    } else {
                        // second request
                        if (alternativeEndPoint) {
                            showSnackBar(R.string.load_book_book_not_found);
                            setActivityState();
                        } else
                            makeRequestBookApi(finalReadIsbn, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setActivityState();
                mProgressBar.setVisibility(View.GONE);
                showSnackBar(R.string.load_book_autofill_request_error);
            }
        });
        jsonRequest.setTag(Constants.ISBN_REQUEST_TAG);
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(jsonRequest);
    }

    private void showSnackBar(int messageResourceId) {
        Snackbar.make(findViewById(R.id.load_book_coordinator_root),
                messageResourceId,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_book_add_photos:
                final CharSequence[] items = {getString(R.string.camera_option_dialog), getString(R.string.gallery_option_dialog)};
                mAlertDialogBuilder = new AlertDialog.Builder(LoadBookActivity.this);
                mAlertDialogBuilder.setNegativeButton(R.string.negative_button_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                mAlertDialogBuilder.setTitle(R.string.title_dialog).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                    try {
                                        mPhotoFile = saveThumbnail();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (mPhotoFile != null) {
                                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                                "com.example.android.fileprovider",
                                                mPhotoFile);
                                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    }
                                    if (!Utilities.checkPermissionActivity(LoadBookActivity.this, Manifest.permission.CAMERA)) {
                                        Utilities.askPermissionActivity(LoadBookActivity.this, Manifest.permission.CAMERA, Constants.CAPTURE_IMAGE);
                                    } else {
                                        startActivityForResult(cameraIntent, Constants.CAPTURE_IMAGE);
                                    }

                                }
                                break;
                            case 1:
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                galleryIntent.setType("image/*");
                                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                                break;
                            default:
                                dialogInterface.cancel();
                        }
                    }
                }).show();
                break;
        }
    }

    @Override
    public void removePhoto(int position) {
        PhotosAdapter adapter = (PhotosAdapter) mPhotosGrid.getAdapter();
        String removed = adapter.removeItem(position);
        if (!removed.contains("content")) {
            File file = new File(removed);
            file.delete();
        }
    }

    public void removePhotoPath(String path) {
        if (!path.contains("content")) {
            File file = new File(path);
            file.delete();
        }
    }

    public File saveThumbnail() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private String generateStorageRef(String path) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String lastPathSegment = Uri.fromFile(new File(path)).getLastPathSegment();
        String lastPathHash = Utilities.getSha1Hex(lastPathSegment);

        return uid + "/images/books/" + lastPathHash + "-" + lastPathSegment + ".jpg";
    }

    private void uploadPhotos(final List<String> userBookPhotosStoragePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        if (userBookPhotosStoragePath.size() > 0) {
            mStorageRef = storageReference.child(generateStorageRef(userBookPhotosStoragePath.get(mUploadedImagesCount)));

            byte[] compressedImage = Utilities.compressPhoto(userBookPhotosStoragePath.get(mUploadedImagesCount), getContentResolver());
            if (compressedImage == null) {
                Toast.makeText(getApplicationContext(),
                        R.string.load_book_upload_error,
                        Toast.LENGTH_SHORT).show();
                cleanUpload();
                return;
            }
            UploadTask uploadTask = mStorageRef.putBytes(compressedImage);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // TODO upload photo, update count, recall method
                    mProgressBar.setProgress(mProgressRate * mUploadedImagesCount + 1);
                    mResultBook.getUserBookPhotosStoragePath().remove(mUploadedImagesCount);
                    mResultBook.getUserBookPhotosStoragePath().add(mUploadedImagesCount, taskSnapshot.getDownloadUrl().toString());
                    if (++mUploadedImagesCount >= userBookPhotosStoragePath.size()) {
                        // upload ended move uploading book info
                        mProgressBar.setIndeterminate(true);
                        uploadBookInfo();
                    } else {
                        uploadPhotos(userBookPhotosStoragePath);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    cleanUpload();
                    showSnackBar(R.string.load_book_upload_error);
                }
            });
        }
    }

    private void cleanUpload() {
        // TODO
    }

    private void uploadBookInfo() {
        mResultBook.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("books").add(mResultBook).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // using documentReference create a folder on storage for storing photos
                //  clean photos
                PhotosAdapter ph = (PhotosAdapter) mPhotosGrid.getAdapter();
                if (ph.getModel() != null)
                    for (String s : ph.getModel()) {
                        removePhotoPath(s);
                    }


                uploadOnAlgloia(mResultBook, documentReference.getId());

                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO clean up
                cleanUpload();
            }
        });
    }

    private void prepareAndStartUpload() {
        mUploading = true;
        //mProgressBar.setVisibility(View.VISIBLE);
        mConditionsSpinner.setEnabled(false);
        mGenreSpinner.setEnabled(false);
        mPositionTextView.setClickable(false);
        mLoanRangeTextView.setClickable(false);
        mAddPhotos.setClickable(false);
        // get photos
        PhotosAdapter photosAdapter = (PhotosAdapter) mPhotosGrid.getAdapter();
        mResultBook.setUserBookPhotosStoragePath(photosAdapter.getModel().size() > 0 ? photosAdapter.getModel() : null);
        mResultBook.setCondition(mConditionsSpinner.getSelectedItemPosition());
        mResultBook.setGenre(mGenreSpinner.getSelectedItemPosition());

        mProgressBar.setVisibility(View.VISIBLE);
        if (mResultBook.getUserBookPhotosStoragePath() != null) {
            mProgressRate = 100 / (mResultBook.getUserBookPhotosStoragePath().size() + 1);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(0);
            mUploadedImagesCount = 0;
            uploadPhotos(mResultBook.getUserBookPhotosStoragePath());
        } else {
            mProgressBar.setIndeterminate(true);
            uploadBookInfo();
        }
    }

    private enum State {
        ISBN_CHOICE, ISBN_MANUALLY, ISBN_ACQUIRED;
    }

    private void setupAlgolia()
    {
        algoliaClient = new Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY);
        algoliaIndex = algoliaClient.getIndex(ALGOLIA_BOOK_INDEX);
    }

    private void uploadOnAlgloia(Book bookToLoad, String bookID) {

        try {

            JSONObject book = new JSONObject();

            book.put("title", bookToLoad.getTitle());
            book.put("author", bookToLoad.getAuthors());
            book.put("publisher", bookToLoad.getPublisher());
            book.put("publishYear", bookToLoad.getPublishYear());
            book.put("conditions", bookToLoad.getCondition());
            book.put("uid", bookToLoad.getUid());
//            book.put("address", bookToLoad.getAddress());

            if(bookToLoad.getIsbn() != null)
            {
                book.put("isbn", bookToLoad.getIsbn());
            }

            if(bookToLoad.getWebThumbnail() != null)
            {
                book.put("thumbnail", bookToLoad.getWebThumbnail());
            }
            else if(bookToLoad.getUserBookPhotosStoragePath().size() > 0)
            {
                book.put("thumbnail", bookToLoad.getUserBookPhotosStoragePath().get(0));
            }

// TODO: set tags
//            if(bookToLoad.tags() != null)
//            {
//                book.put("tags", bookToLoad.getTags());
//            }

            algoliaIndex.addObjectAsync(book, bookID,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
}
