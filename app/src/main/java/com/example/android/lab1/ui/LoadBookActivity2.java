package com.example.android.lab1.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.PhotosAdapter;
import com.example.android.lab1.adapter.RemovePhotoClickListener;
import com.example.android.lab1.model.Address;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.ui.listeners.PositionActivityOpenerListener;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.ganfra.materialspinner.MaterialSpinner;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.example.android.lab1.utils.Constants.RESULT_LOAD_IMAGE;
import static com.example.android.lab1.utils.Constants.SCAN_REQUEST_TAG;

public class LoadBookActivity2 extends AppCompatActivity implements View.OnClickListener, RemovePhotoClickListener {

    private static final String ACTIVITY_STATE = "STATE";
    private static final String RESULT_BOOK = "RESULT";
    private static final String PHOTOS_KEY = "USER_PHOTOS";
    private static final String PUBLISH_YEAR_SPINNER = "PUBLISH_YEAR_SPINNER";
    private static final String CONDITION_SPINNER = "CONDITION_SPINNER";
    private static final String GENRE_SPINNER = "GENRE_SPINNER";
    private static final String ISBN = "ISBN";
    private static final String CURRENT_PHOTO = "CURRENT_PHOTO";
    private static final String KEY_RECYCLER_STATE = "KEY_RECYCLER";
    private Toolbar mToolbar;
    private Toolbar mIsbnToolbar;
    private MaterialSpinner mPublishYearSpinner;
    private MaterialSpinner mConditionsSpinner;
    private MaterialSpinner mGenreSpinner;
    private EditText mIsbnEditText;

    private NestedScrollView mInfoContainerScrollView;
    private ImageView mBookWebThumbnailImageView;
    private TextInputLayout mTitleTextInputLayout;
    private LinearLayoutCompat mAuthorsListView;
    private TextInputLayout mPublisherTextInputLayout;
    private AppCompatButton mPositionTextView;
    private AppCompatButton mLoanRangeTextView;
    private AppCompatButton mScanBarcodeButton;
    private AppCompatButton mInsertManuallyButton;
    private ImageView mIsbnExplanationImageView;
    private RequestQueue mRequestQueue;
    private ImageButton mAddAuthorButton;
    private ImageButton mRemoveAuthorButton;
    private ProgressBar mProgressBar;
    private RecyclerView mPhotosGrid;
    private ImageButton mAddPhotos;

    private String mCurrentTitle;
    private Book mResultBook;

    private AlertDialog.Builder mAlertDialogBuilder;
    private File mPhotoFile;
    private State mActivityState;
    private Bundle mBundleRecyclerViewState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book_2);
        initializeWidgets();

        mActivityState = State.ISBN_CHOICE;
        setActivityState();

        setupToolBar();
        setupSpinners();
        setupIsbnListeners();
        setupListeners();
        setupGallery();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivityState != null)
            outState.putString(ACTIVITY_STATE, mActivityState.toString());
        if (mActivityState.equals(State.ISBN_MANUALLY) && mIsbnEditText != null)
            outState.putString(ISBN, mIsbnEditText.getText().toString());

        if (mResultBook != null)
            outState.putSerializable(RESULT_BOOK, mResultBook);
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mPhotosGrid.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mPhotosGrid.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(ACTIVITY_STATE))
            mActivityState = State.valueOf(savedInstanceState.getString(ACTIVITY_STATE));
        if (savedInstanceState.containsKey(ISBN) && mIsbnEditText != null)
            mIsbnEditText.setText(savedInstanceState.getString(ISBN));
        if (savedInstanceState.containsKey(CURRENT_PHOTO))
            mPhotoFile = new File(savedInstanceState.getString(CURRENT_PHOTO));
        if (savedInstanceState.containsKey(RESULT_BOOK)) {
            mResultBook = (Book) savedInstanceState.getSerializable(RESULT_BOOK);
            if (mResultBook.getWebThumbnail() != null && mBookWebThumbnailImageView != null)
                Glide.with(getApplicationContext())
                        .load(mResultBook.getWebThumbnail())
                        .into(mBookWebThumbnailImageView);

            if (mResultBook.getIsbn() != null && mIsbnEditText != null)
                mIsbnEditText.setText(mResultBook.getIsbn());
            if (mResultBook.getTitle() != null) {
                mCurrentTitle = mResultBook.getTitle();
                if (mTitleTextInputLayout != null && mTitleTextInputLayout.getEditText() != null) {
                    mTitleTextInputLayout.getEditText().setText(mCurrentTitle);
                    mTitleTextInputLayout.getEditText().setEnabled(false);
                }
            }
            for (int i = 0; i < mResultBook.getAuthors().size(); i++) {
                if (i == 0)
                    setAuthorViewText(mResultBook.getAuthors().get(0), i);
                else
                    createAuthorView(mResultBook.getAuthors().get(i));
            }

            if (mResultBook.getPublisher() != null && mPublisherTextInputLayout != null &&
                    mPublisherTextInputLayout.getEditText() != null) {
                mPublisherTextInputLayout.getEditText().setText(mResultBook.getPublisher());
                mPublisherTextInputLayout.getEditText().setEnabled(false);
            }

            if (mResultBook.getPublishYear() != null && mPublishYearSpinner != null)
                mPublishYearSpinner.setEnabled(false);

            if (mResultBook.getAddress() != null && mPositionTextView != null)
                mPositionTextView.setText(mResultBook.getAddress().getAddress());
        }

        if (savedInstanceState.containsKey(PUBLISH_YEAR_SPINNER) && mPublishYearSpinner != null)
            mPublishYearSpinner.setSelection(savedInstanceState.getInt(PUBLISH_YEAR_SPINNER));
        if (savedInstanceState.containsKey(CONDITION_SPINNER) && mConditionsSpinner != null)
            mConditionsSpinner.setSelection(savedInstanceState.getInt(CONDITION_SPINNER));
        if (savedInstanceState.containsKey(GENRE_SPINNER) && mGenreSpinner != null)
            mGenreSpinner.setSelection(savedInstanceState.getInt(GENRE_SPINNER));
        /*if (savedInstanceState.containsKey(PHOTOS_KEY) && mPhotosGrid != null)
        {
            mPhotosGrid.
            for (String photo : savedInstanceState.getStringArrayList(PHOTOS_KEY))
                ((PhotosAdapter) mPhotosGrid.getAdapter()).addItem(photo);
        }*/
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
                mIsbnToolbar.setVisibility(View.VISIBLE);
                findViewById(R.id.load_book_isbn).setVisibility(View.VISIBLE);
                mIsbnEditText.setVisibility(View.VISIBLE);
                mScanBarcodeButton.setVisibility(View.GONE);
                mIsbnExplanationImageView.setVisibility(View.VISIBLE);
                mInfoContainerScrollView.setVisibility(View.GONE);
                mIsbnEditText.requestFocus();
                break;
            case ISBN_ACQUIRED:
                mProgressBar.setVisibility(View.GONE);
                mIsbnToolbar.setVisibility(View.GONE);
                mIsbnEditText.setEnabled(false);
                mIsbnExplanationImageView.setVisibility(View.GONE);
                mInfoContainerScrollView.setVisibility(View.VISIBLE);
                break;
            default:
                mCurrentTitle = getString(R.string.insert_isbn_code);
                mInsertManuallyButton.setText(R.string.insert_manually);
                mInsertManuallyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivityState = State.ISBN_MANUALLY;
                        setActivityState();
                    }
                });
                mIsbnToolbar.setVisibility(View.VISIBLE);
                mScanBarcodeButton.setVisibility(View.VISIBLE);
                findViewById(R.id.load_book_isbn).setVisibility(View.GONE);
                mIsbnExplanationImageView.setVisibility(View.VISIBLE);
                mInfoContainerScrollView.setVisibility(View.GONE);
                break;
        }

        mToolbar.setTitle(mCurrentTitle);
    }

    private void setupGallery() {
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(this);
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPhotosGrid.setLayoutManager(MyLayoutManager);
        mPhotosGrid.setAdapter(new PhotosAdapter(this));
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
                    mResultBook.setAddress(address);
                    mPositionTextView.setText(address.getAddress());
                }
                break;
        }
    }

    private void setupListeners() {
        mAddAuthorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAuthorView(null);
            }
        });

        mRemoveAuthorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAuthorView();
            }
        });

        mPositionTextView.setOnClickListener(new PositionActivityOpenerListener(this,
                false));

        mScanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.startBarcodeScanner(LoadBookActivity2.this);
            }
        });

        mAddPhotos.setOnClickListener(this);

    }

    private void initializeWidgets() {
        mToolbar = findViewById(R.id.toolbar);
        TextInputLayout isbnTextContainer = findViewById(R.id.load_book_isbn);
        mIsbnEditText = isbnTextContainer.getEditText();
        mIsbnToolbar = findViewById(R.id.load_book_toolbar_isbn);
        mInsertManuallyButton = findViewById(R.id.load_book_search_manually);
        mIsbnExplanationImageView = findViewById(R.id.load_book_isbn_explanation);
        mInfoContainerScrollView = findViewById(R.id.load_book_info_container);
        mBookWebThumbnailImageView = findViewById(R.id.thumbnail);
        mTitleTextInputLayout = findViewById(R.id.load_book_title);
        mAuthorsListView = findViewById(R.id.load_book_author_list);
        mPublisherTextInputLayout = findViewById(R.id.load_book_publisher);
        mPositionTextView = findViewById(R.id.load_book_position);
        mLoanRangeTextView = findViewById(R.id.load_book_loan_range);
        mScanBarcodeButton = findViewById(R.id.load_book_scan_bar_code);
        mPublishYearSpinner = findViewById(R.id.load_book_publish_year_spinner);
        mConditionsSpinner = findViewById(R.id.load_book_conditions_spinner);
        mGenreSpinner = findViewById(R.id.load_book_genre);
        mAddAuthorButton = findViewById(R.id.load_book_add_author);
        mRemoveAuthorButton = findViewById(R.id.load_book_remove_author);
        mProgressBar = findViewById(R.id.load_book_progress_bar);
        mPhotosGrid = findViewById(R.id.load_book_loaded_photos);
        mAddPhotos = findViewById(R.id.load_book_add_photos);
    }

    private void setupToolBar() {
        // toolbar
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
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
    protected void onResume() {
        super.onResume();
    }

    private void setupIsbnListeners() {
        // scan bar code
        mScanBarcodeButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                                              Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                          Utilities.startBarcodeScanner(LoadBookActivity2.this);
                                                      } else {
                                                          ActivityCompat.requestPermissions(LoadBookActivity2.this,
                                                                  new String[]{Manifest.permission.CAMERA},
                                                                  SCAN_REQUEST_TAG);
                                                      }
                                                  }
                                              }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SCAN_REQUEST_TAG:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Utilities.startBarcodeScanner(LoadBookActivity2.this);
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
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("totalItems") > 0) {
                        mResultBook = new Book();
                        JSONObject book = response.getJSONArray("items")
                                .getJSONObject(0).getJSONObject("volumeInfo");

                        mResultBook.setIsbn(mIsbnEditText.getText().toString());

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
                        if (mPublisherTextInputLayout.getEditText().getText() != null &&
                                mPublisherTextInputLayout.getEditText().getText().length() > 0)
                            mPublisherTextInputLayout.getEditText().setEnabled(false);

                        if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0) {
                            JSONArray authors = book.getJSONArray("authors");
                            int numOfViews = authors.length() > 5 ? 5 : authors.length();
                            int actualViews = getAuthorsViewsCount();
                            String curAuthor;
                            for (int i = 0; i < numOfViews; i++) {
                                curAuthor = authors.getString(i);
                                mResultBook.addAuthor(curAuthor);
                                if (i < actualViews)
                                    setAuthorViewText(curAuthor, i);
                                else
                                    createAuthorView(curAuthor);
                            }
                            mAddAuthorButton.setVisibility(View.GONE);
                            mRemoveAuthorButton.setVisibility(View.GONE);
                        }

                        if (book.has("publishedDate")) {
                            mResultBook.setPublishYear(book.getInt("publishedDate"));
                            int currentYear, publishYear;
                            currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            publishYear = mResultBook.getPublishYear();
                            mPublishYearSpinner.setSelection(currentYear - publishYear);
                            mPublishYearSpinner.setEnabled(false);
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

                        setActivityState();
                        mTitleTextInputLayout.getEditText().clearFocus();
                        mPublishYearSpinner.clearFocus();
                        mPublishYearSpinner.clearFocus();
                        mInfoContainerScrollView.requestFocus();

                    } else {
                        // second request
                        if (alternativeEndPoint) {
                            showSnackbar(R.string.load_book_book_not_found);
                            setActivityState();
                        } else
                            makeRequestBookApi(isbn, true);
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
                showSnackbar(R.string.load_book_autofill_request_error);
            }
        });
        jsonRequest.setTag(Constants.ISBN_REQUEST_TAG);
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(jsonRequest);
    }

    private void setAuthorViewText(String string, int i) {
        try {
            TextInputLayout childAt = (TextInputLayout) mAuthorsListView.getChildAt(i);
            childAt.getEditText().setText(string);
            childAt.setEnabled(false);
        } catch (NullPointerException ex) {

        } catch (ClassCastException ex1) {

        }
    }

    private int getAuthorsViewsCount() {
        return mAuthorsListView.getChildCount() - 1;
    }

    private void createAuthorView(String authorName) {
        if (getAuthorsViewsCount() < 5) {
            LayoutInflater l = getLayoutInflater();
            TextInputLayout newEditText = (TextInputLayout) l.inflate(R.layout.author_edit_text, null);
            if (newEditText != null && newEditText.getEditText() != null)
                newEditText.getEditText().setText(authorName);
            newEditText.setEnabled(authorName == null);
            mAuthorsListView.addView(newEditText, getAuthorsViewsCount() - 1);
            if (getAuthorsViewsCount() < 2)
                mRemoveAuthorButton.setVisibility(View.GONE);
            else
                mRemoveAuthorButton.setVisibility(View.VISIBLE);
        }
    }

    private void removeAuthorView() {
        if (getAuthorsViewsCount() > 1) {
            mAuthorsListView.removeViewAt(getAuthorsViewsCount() - 1);
            if (getAuthorsViewsCount() == 1)
                mRemoveAuthorButton.setVisibility(View.GONE);
        }

    }

    private void showSnackbar(int messageResourceId) {
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
                mAlertDialogBuilder = new AlertDialog.Builder(LoadBookActivity2.this);
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
                                    if (!Utilities.checkPermissionActivity(LoadBookActivity2.this, Manifest.permission.CAMERA)) {
                                        Utilities.askPermissionActivity(LoadBookActivity2.this, Manifest.permission.CAMERA, Constants.CAPTURE_IMAGE);
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

    private enum State {
        ISBN_CHOICE, ISBN_MANUALLY, ISBN_ACQUIRED;
    }
}
