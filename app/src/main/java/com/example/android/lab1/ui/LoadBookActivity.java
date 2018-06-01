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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.example.android.lab1.adapter.TagsAdapter;
import com.example.android.lab1.model.Address;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.ui.listeners.OnPhotoClickListenerDefaultImpl;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import fr.ganfra.materialspinner.MaterialSpinner;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.example.android.lab1.utils.Constants.POSITION_ACTIVITY_REQUEST;
import static com.example.android.lab1.utils.Constants.RESULT_LOAD_IMAGE;
import static com.example.android.lab1.utils.Constants.SCAN_REQUEST_TAG;

public class LoadBookActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_API_KEY = "36664d38d1ffa619b47a8b56069835d1";
    private final static String ALGOLIA_BOOK_INDEX = "books";
    private static final String ACTIVITY_STATE = "ACTIVITY_STATE";
    public static final String BOOK = "BOOK";
    private static final String PHOTOS_KEY = "PHOTOS_KEY";
    private static final String PUBLISH_YEAR_SPINNER = "PUBLISH_YEAR_SPINNER";
    private static final String CONDITION_SPINNER = "CONDITION_SPINNER";
    private static final String GENRE_SPINNER = "GENRE_SPINNER";
    private static final String ISBN = "ISBN";
    private static final String CURRENT_PHOTO = "CURRENT_PHOTO";
    private static final String UPLOADING = "UPLOADING";
    private static final String AUTHORS = "AUTHORS";
    private static final String AUTHORS_EDITABLE = "AUTHORS_EDITABLE";
    private static final String TAGS = "TAGS";
    private static final String EDITING = "EDITING";
    private static final String MARKED_TO_BE_REMOVED = "MARKED_TO_BE_REMOVED";
    private static final String CURRENT_TAG = "CURRENT_TAG";
    private static final String CURRENT_PUBLISHER = "CURRENT_PUBLISHER";
    private static final int ISBN_CHOICE = 1;
    private static final int ISBN_MANUALLY = 2;
    private static final int ISBN_ACQUIRED = 3;
    private static final int EDIT_BOOK = 4;
    /*Algolia connection variables*/
    Client algoliaClient;
    Index algoliaIndex;
    private View mAddPhotosTextView;
    private View mInfoContainerScrollView;
    private TextInputLayout mTagsTextInputLayout;
    private Toolbar mToolbar;
    private LinearLayoutCompat mIsbnContainer;
    private MaterialSpinner mPublishYearSpinner;
    private MaterialSpinner mConditionsSpinner;
    private MaterialSpinner mGenreSpinner;
    private EditText mIsbnEditText;
    private List<String> mPhotosPath;
    private ImageView mBookWebThumbnailImageView;
    private TextView mTitleTextView;
    private RecyclerView mAuthorsListView;
    private TextInputLayout mPublisherTextInputLayout;
    private TextView mPositionTextView;
    private AppCompatButton mScanBarcodeButton;
    private AppCompatButton mInsertManuallyButton;
    private ImageView mIsbnExplanationImageView;
    private RequestQueue mRequestQueue;
    private ProgressBar mProgressBar;
    private RecyclerView mTagsRecyclerView;
    private RecyclerView mPhotosRecyclerView;
    private String mCurrentTitle;
    private Book mResultBook;
    private List<String> mTags;
    private File mPhotoFile;
    private Integer mActivityState;
    private boolean mUploading;
    private AppCompatButton mConfirmButton;
    private int mProgressRate;
    private int mUploadedImagesCount;
    private Book mBeforeEditBook;
    private ArrayList<String> mMarkedToBeRemoved;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMarkedToBeRemoved != null)
            outState.putStringArrayList(MARKED_TO_BE_REMOVED,
                    mMarkedToBeRemoved);
        if (mBeforeEditBook != null)
            outState.putParcelable(EDITING, mBeforeEditBook);
        if (mActivityState != null)
            outState.putInt(ACTIVITY_STATE, mActivityState);
        if (mActivityState != null && mActivityState.equals(ISBN_MANUALLY) && mIsbnEditText != null)
            outState.putString(ISBN, mIsbnEditText.getText().toString());
        if (mResultBook != null)
            outState.putParcelable(BOOK, mResultBook);
        if (mPhotosPath != null)
            outState.putStringArrayList(PHOTOS_KEY, (ArrayList<String>) mPhotosPath);
        if (mTags != null)
            outState.putStringArrayList(TAGS, (ArrayList<String>) mTags);
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
                outState.putBoolean(AUTHORS_EDITABLE, ph.isEditable());
            }
        }
        if (mTagsTextInputLayout != null && mTagsTextInputLayout.getEditText() != null)
            outState.putString(CURRENT_TAG, mTagsTextInputLayout.getEditText()
                    .getText()
                    .toString());
        if (mPublisherTextInputLayout != null && mPublisherTextInputLayout.getEditText() != null)
            outState.putString(CURRENT_PUBLISHER, mPublisherTextInputLayout.getEditText()
                    .getText()
                    .toString());
        outState.putBoolean(UPLOADING, mUploading);
    }

    private void errorDownloadApi(boolean bookNotFound) {
        updateActivityState();
        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),
                bookNotFound ? R.string.load_book_book_not_found :
                        R.string.load_book_autofill_request_error,
                Toast.LENGTH_SHORT).show();
    }

    /*private void showSnackBar(int messageResourceId) {
        Snackbar.make(findViewById(R.id.load_book_coordinator_root),
                messageResourceId,
                Snackbar.LENGTH_SHORT)
                .show();
    }*/

    private void makeRequestBookApi(final String isbn, final boolean alternativeEndPoint) {
        mInsertManuallyButton.setOnClickListener(null);
        if (mActivityState == ISBN_MANUALLY)
            Utilities.hideKeyboard(getApplicationContext(), LoadBookActivity.this);
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
                                .getJSONObject(0)
                                .getJSONObject("volumeInfo");
                        // get complete isbn
                        if (book.has("industryIdentifiers")) {
                            JSONArray industryIdentifiers = book.getJSONArray("industryIdentifiers");
                            for (int i = 0; i < industryIdentifiers.length(); i++) {
                                if (industryIdentifiers.getJSONObject(i)
                                        .getString("type")
                                        .equals("ISBN_13"))
                                    mResultBook.setIsbn(industryIdentifiers.getJSONObject(i)
                                            .getString("identifier"));
                            }
                            if (mResultBook.getIsbn() == null) {
                                errorDownloadApi(true);
                                return;
                            }
                        } else {
                            errorDownloadApi(true);
                        }
                        // book with no title not accepted
                        mResultBook.setTitle(book.optString("title"));
                        if (mResultBook.getTitle() == null || mResultBook.getTitle()
                                .length() <= 0) {
                            errorDownloadApi(true);
                            return;
                        }

                        mResultBook.setDescription(book.has("description") ?
                                book.getString("description") : null);

                        mResultBook.setPublisher(book.has("publisher") ?
                                book.getString("publisher") : null);

                        if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0) {
                            JSONArray authors = book.getJSONArray("authors");
                            for (int i = 0; i < authors.length(); i++) {
                                mResultBook.addAuthor(authors.getString(i));
                            }
                        }

                        if (book.has("publishedDate")) {
                            String publishedDate = book.getString("publishedDate");
                            if (publishedDate.contains("-"))
                                mResultBook.setPublishYear(Integer.parseInt
                                        (publishedDate.split("-")[0]));
                            else
                                mResultBook.setPublishYear(book.getInt("publishedDate"));
                        }


                        if (book.has("imageLinks")) {
                            JSONObject imageLinks = book.getJSONObject("imageLinks");
                            if (imageLinks.length() > 0 &&
                                    imageLinks.has("thumbnail")) {
                                mResultBook.setWebThumbnail(imageLinks.getString("thumbnail"));
                            }
                        }

                        if (book.has("infoLink"))
                            mResultBook.setInfoLink(book.getString("infoLink"));
                        else if (book.has("canonicalVolumeLink"))
                            mResultBook.setInfoLink(book.getString("canonicalVolumeLink"));

                        mActivityState = ISBN_ACQUIRED;
                        mTags = new ArrayList<>();
                        mTagsRecyclerView.setAdapter(new TagsAdapter(mTags));
                        mPhotosPath = new ArrayList<>();
                        mPhotosRecyclerView.setAdapter(new PhotosAdapter(mPhotosPath));
                        updateUI(mResultBook);
                        updateActivityState();
                    } else {
                        // second request
                        if (alternativeEndPoint) {
                            errorDownloadApi(true);
                            updateActivityState();
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
                errorDownloadApi(false);
            }
        });
        jsonRequest.setTag(Constants.ISBN_REQUEST_TAG);
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(jsonRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book);

        // urgent InstanceState to restore
        if (savedInstanceState != null) {
            mActivityState = savedInstanceState.getInt(ACTIVITY_STATE);
            mPhotosPath = savedInstanceState.getStringArrayList(PHOTOS_KEY);
        }
        // check if edit mode
        if (getIntent() != null &&
                getIntent().hasExtra(BOOK) &&
                mActivityState == null) {
            // edit book
            mBeforeEditBook = getIntent().getParcelableExtra(BOOK);
            mResultBook = new Book(mBeforeEditBook);
            mActivityState = EDIT_BOOK;
            if (mResultBook.getUserBookPhotosStoragePath() != null)
                mPhotosPath = mResultBook.getUserBookPhotosStoragePath();
            if (mResultBook.getTags() != null)
                mTags = new ArrayList<>(Arrays
                        .asList(TextUtils.split(mResultBook.getTags(), " ")));

        } else if (mActivityState == null) {
            mActivityState = ISBN_CHOICE;
        }
        initializeWidgets();
        if (mBeforeEditBook != null && savedInstanceState == null)
            updateUI(mBeforeEditBook);
        mUploading = false;


        updateActivityState();
        Utilities.setupStatusBarColor(this);
        setupToolBar();
        setupSpinners();
        setupGallery();
        setupListeners();
        setupAlgolia();
    }

    private void updateUI(Book book) {
        // title
        mCurrentTitle = book.getTitle();
        mTitleTextView.setText(mCurrentTitle);
        // publisher
        if (mPublisherTextInputLayout != null
                && mPublisherTextInputLayout.getEditText() != null
                && book.getPublisher() != null
                && book.getPublisher().length() > 0) {
            mPublisherTextInputLayout.getEditText().setText(book.getPublisher());
            mPublisherTextInputLayout.getEditText().setEnabled(false);
        } else if (mPublisherTextInputLayout != null
                && mPublisherTextInputLayout.getEditText() != null)
            mPublisherTextInputLayout.getEditText().setEnabled(true);
        // authors
        if (book.getAuthors() != null) {
            ArrayList<String> authorsList = new ArrayList<>();
            Collections.addAll(authorsList, book.getAuthors().split(","));
            AuthorAdapter authorAdapter = new AuthorAdapter(getApplicationContext(),
                    authorsList);
            authorAdapter.setEditable(false);
            mAuthorsListView.setAdapter(authorAdapter);
            mAuthorsListView.getAdapter().notifyDataSetChanged();
        } else {
            AuthorAdapter authorAdapter = new AuthorAdapter(getApplicationContext(), null);
            authorAdapter.setEditable(true);
            mAuthorsListView.setAdapter(authorAdapter);
        }
        // publish year
        if (book.getPublishYear() != null) {
            int currentYear, publishYear;
            currentYear = Calendar.getInstance().get(Calendar.YEAR);
            publishYear = book.getPublishYear();
            mPublishYearSpinner.setSelection(1 + currentYear - publishYear);
            mPublishYearSpinner.setEnabled(false);
        } else {
            mPublishYearSpinner.setSelection(0);
            mPublishYearSpinner.setEnabled(true);
        }
        // thumbnail
        if (book.getWebThumbnail() != null) {
            Glide.with(getApplicationContext())
                    .load(book.getWebThumbnail())
                    .into(mBookWebThumbnailImageView);
        } else
            Glide.with(getApplicationContext())
                    .load(R.drawable.book_thumbnail_placeholder)
                    .into(mBookWebThumbnailImageView);
        // conditions
        if (book.getCondition() != null)
            mConditionsSpinner.setSelection(book.getCondition() + 1);
        if (book.getGenre() != null)
            mGenreSpinner.setSelection(book.getGenre() + 1);
        if (book.getGeoPoint() != null)
            mPositionTextView.setText(book.getAddress());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMarkedToBeRemoved = savedInstanceState.getStringArrayList(MARKED_TO_BE_REMOVED);
        mBeforeEditBook = savedInstanceState.getParcelable(EDITING);
        if (mBeforeEditBook != null) updateUI(mBeforeEditBook);
        setUploading(savedInstanceState.getBoolean(UPLOADING));
        mActivityState = savedInstanceState.getInt(ACTIVITY_STATE);
        if (mIsbnEditText != null)
            mIsbnEditText.setText(savedInstanceState.getString(ISBN));
        mTags = savedInstanceState.getStringArrayList(TAGS);
        if (mTags == null)
            mTags = new ArrayList<>();
        mTagsRecyclerView.setAdapter(new TagsAdapter(mTags));
        mPhotoFile = savedInstanceState.containsKey(CURRENT_PHOTO) ?
                new File(Objects.requireNonNull(savedInstanceState.getString(CURRENT_PHOTO))) : null;

        if ((mActivityState.equals(ISBN_ACQUIRED) || mActivityState.equals(EDIT_BOOK)) &&
                savedInstanceState.containsKey(BOOK)) {
            mResultBook = savedInstanceState.getParcelable(BOOK);
            if (mResultBook != null) {
                if (mResultBook.getWebThumbnail() != null && mBookWebThumbnailImageView != null)
                    Glide.with(getApplicationContext())
                            .load(mResultBook.getWebThumbnail())
                            .into(mBookWebThumbnailImageView);
                else if (mBookWebThumbnailImageView != null)
                    Glide.with(getApplicationContext())
                            .load(R.drawable.book_thumbnail_placeholder)
                            .into(mBookWebThumbnailImageView);

                if (mIsbnEditText != null && mResultBook.getIsbn() != null &&
                        mResultBook.getIsbn().length() > 0)
                    mIsbnEditText.setText(mResultBook.getIsbn());
                mCurrentTitle = mResultBook.getTitle();
                if (mTitleTextView != null) {
                    mTitleTextView.setText(mCurrentTitle);
                }

                if (mResultBook.getAuthors() != null) {
                    String[] authors = mResultBook.getAuthors().split(",");
                    ArrayList<String> authorsList = new ArrayList<>();
                    Collections.addAll(authorsList, authors);
                    mAuthorsListView.setAdapter(new AuthorAdapter(getApplicationContext(), authorsList));
                }

                if (mResultBook.getPublisher() != null && mPublisherTextInputLayout != null &&
                        mPublisherTextInputLayout.getEditText() != null) {
                    mPublisherTextInputLayout.getEditText().setText(mResultBook.getPublisher());
                    mPublisherTextInputLayout.getEditText().setEnabled(false);
                } else if (mPublisherTextInputLayout != null && mPublisherTextInputLayout.getEditText() != null &&
                        savedInstanceState.containsKey(CURRENT_PUBLISHER))
                    mPublisherTextInputLayout.getEditText().setText(savedInstanceState.getString(CURRENT_PUBLISHER));

                if (mResultBook.getPublishYear() != null && mPublishYearSpinner != null) {
                    int currentYear, publishYear;
                    currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    publishYear = mResultBook.getPublishYear();
                    mPublishYearSpinner.setSelection(currentYear + 1 - publishYear);
                    mPublishYearSpinner.setEnabled(false);
                } else if (mPublishYearSpinner != null)
                    mPublishYearSpinner.setEnabled(true);

                if (mPositionTextView != null)
                    mPositionTextView.setText(mResultBook.getAddress());
            }
            if (savedInstanceState.containsKey(CONDITION_SPINNER) && mConditionsSpinner != null)
                mConditionsSpinner.setSelection(savedInstanceState.getInt(CONDITION_SPINNER));
            if (savedInstanceState.containsKey(GENRE_SPINNER) && mGenreSpinner != null)
                mGenreSpinner.setSelection(savedInstanceState.getInt(GENRE_SPINNER));

            if (savedInstanceState.containsKey(AUTHORS)
                    && savedInstanceState.containsKey(AUTHORS_EDITABLE)) {
                AuthorAdapter authorAdapter = new AuthorAdapter(getApplicationContext(),
                        savedInstanceState.getStringArrayList(AUTHORS));
                authorAdapter.setEditable(savedInstanceState.getBoolean(AUTHORS_EDITABLE));
                mAuthorsListView.setAdapter(authorAdapter);
            }
        }
        updateActivityState();
    }

    private void updateActivityState() {
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
                            mToolbar.requestFocus();
                            mPublishYearSpinner.clearFocus();
                            // make google api request
                            makeRequestBookApi(mIsbnEditText.getText().toString(), false);
                        }
                    }
                });
                mIsbnContainer.setVisibility(View.VISIBLE);
                //mIsbnEditText.setText(null);
                TextInputLayout isbnContainer = findViewById(R.id.load_book_isbn);
                isbnContainer.setVisibility(View.VISIBLE);
                mIsbnEditText.setVisibility(View.VISIBLE);
                mScanBarcodeButton.setVisibility(View.GONE);
                mIsbnExplanationImageView.setVisibility(View.VISIBLE);
                mInfoContainerScrollView.setVisibility(View.GONE);
                mInfoContainerScrollView.setScrollY(0);
                mProgressBar.setVisibility(View.GONE);
                break;
            case EDIT_BOOK:
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
                mIsbnEditText.setText(null);
                mInsertManuallyButton.setText(R.string.insert_manually);
                mInsertManuallyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivityState = ISBN_MANUALLY;
                        Utilities.showSoftKeyboard(getApplicationContext(), mIsbnEditText);
                        updateActivityState();
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
                mIsbnExplanationImageView.requestFocus();
                Utilities.hideKeyboard(getApplicationContext(), LoadBookActivity.this);
                cleanPhotos();
                break;
        }

        mToolbar.setTitle(mCurrentTitle);
    }

    private void setupGallery() {
        if (mPhotosRecyclerView.getAdapter() == null) {
            LinearLayoutManager myLayoutManager = new LinearLayoutManager(this);
            myLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mPhotosRecyclerView.setLayoutManager(myLayoutManager);
            //mPhotosRecyclerView.setHasFixedSize(true);
            if (mPhotosPath == null)
                mPhotosPath = new ArrayList<>();
            PhotosAdapter photosAdapter = new PhotosAdapter(mPhotosPath);
            photosAdapter.setPhotoClickListener(new OnPhotoClickListenerDefaultImpl(this,
                    mPhotosPath,
                    mCurrentTitle));
            mPhotosRecyclerView.setAdapter(photosAdapter);
        }
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
                    if (mPhotosPath.size() < getApplicationContext()
                            .getResources().getInteger(R.integer.max_photos)) {
                        mPhotosPath.add(mPhotoFile.getAbsolutePath());
                        mPhotosRecyclerView.getAdapter().notifyDataSetChanged();
                    } else
                        Toast.makeText(this,
                                R.string.max_photos,
                                Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED && mPhotoFile != null) {
                    if (mPhotoFile.isFile())
                        removePhotoPath(mPhotoFile.getAbsolutePath());
                }
                break;
            case Constants.RESULT_LOAD_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.getData() != null) {
                        if (mPhotosPath.size() < getApplicationContext()
                                .getResources().getInteger(R.integer.max_photos)) {
                            mPhotosPath.add(data.getData().toString());
                            mPhotosRecyclerView.getAdapter().notifyDataSetChanged();
                        }
                    } else
                        Toast.makeText(this,
                                R.string.max_photos,
                                Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.POSITION_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    Address address = Utilities.readResultOfPositionActivity(data);
                    mResultBook.setAddress(address != null ? address.getAddress() : null);
                    mResultBook.setGeoPoint(address != null ? address.getLat() : null, address != null ? address.getLon() : null);
                    mPositionTextView.setText(address != null ? address.getAddress() : null);
                }
                break;
        }
    }

    private void setupListeners() {

        mPositionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.checkPermissionActivity(LoadBookActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent i = Utilities.getPositionActivityIntent(LoadBookActivity.this,
                            false);
                    startActivityForResult(i, Constants.POSITION_ACTIVITY_REQUEST);
                } else {
                    Utilities.askPermissionActivity(LoadBookActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Constants.POSITION_ACTIVITY_REQUEST);
                }

            }
        });

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

        mAddPhotosTextView.setOnClickListener(this);

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
                            mIsbnExplanationImageView.requestFocus();
                            Utilities.hideKeyboard(v.getContext(), LoadBookActivity.this);
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
                else {
                    v.setClickable(true);
                }
            }
        });

        mTagsTextInputLayout.getEditText().setFilters(new InputFilter[]
                {
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                for (int i = start; i < end; i++) {
                                    if (source.charAt(i) == ' ')
                                        continue;
                                    if (!Character.isLetter(source.charAt(i)))
                                        return "";
                                }
                                return null;
                            }
                        }
                });

        mTagsTextInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
                    mTags.add(text.trim());
                    mTagsRecyclerView.getAdapter().notifyItemInserted(mTags.size() - 1);
                    mTagsRecyclerView.scrollToPosition(mTags.size() - 1);
                    mTagsTextInputLayout.getEditText().setText(null);
                    //Utilities.hideKeyboard(getApplicationContext(), LoadBookActivity.this);
                }
            }
        });
        mTagsTextInputLayout.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (((actionId == EditorInfo.IME_ACTION_DONE) ||
                        (event != null &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        && mTagsTextInputLayout != null &&
                        mTagsTextInputLayout.getEditText().getText() != null &&
                        mTagsTextInputLayout.getEditText().getText().length() > 0) {
                    mTags.add(mTagsTextInputLayout.getEditText().getText().toString().trim());
                    mTagsRecyclerView.getAdapter().notifyItemInserted(mTags.size() - 1);
                    mTagsRecyclerView.scrollToPosition(mTags.size() - 1);
                    mTagsTextInputLayout.getEditText().setText(null);
                    Utilities.hideKeyboard(getApplicationContext(), LoadBookActivity.this);
                }

                return false;
            }
        });

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
        mTitleTextView = findViewById(R.id.load_book_title);
        mAuthorsListView = findViewById(R.id.load_book_author_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        mTagsRecyclerView = findViewById(R.id.load_book_tags_rv);
        if (mTags == null)
            mTags = new ArrayList<>();
        mTagsRecyclerView.setAdapter(new TagsAdapter(mTags));
        mTagsRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false));
        mAuthorsListView.setHasFixedSize(true);
        mAuthorsListView.setLayoutManager(layoutManager);
        mPublisherTextInputLayout = findViewById(R.id.load_book_publisher);
        mPositionTextView = findViewById(R.id.load_book_position);
        mScanBarcodeButton = findViewById(R.id.load_book_scan_bar_code);
        mPublishYearSpinner = findViewById(R.id.load_book_publish_year_spinner);
        mConditionsSpinner = findViewById(R.id.load_book_conditions_spinner);
        mGenreSpinner = findViewById(R.id.load_book_genre);
        mProgressBar = findViewById(R.id.load_book_progress_bar);
        mPhotosRecyclerView = findViewById(R.id.load_book_loaded_photos);
        mConfirmButton = findViewById(R.id.load_book_confirm_book);
        if (mActivityState.equals(EDIT_BOOK))
            mConfirmButton.setText(R.string.modify_book);
        mTagsTextInputLayout = findViewById(R.id.load_book_tags_edit_view);
        mAddPhotosTextView = findViewById(R.id.add_photos_text_view);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!mUploading) {
            if (mActivityState.equals(ISBN_MANUALLY) || mActivityState.equals(ISBN_ACQUIRED)) {
                if (mActivityState.equals(ISBN_ACQUIRED)) {
                    cleanPhotos();

                }
                mIsbnEditText.setText(null);
                mActivityState = ISBN_CHOICE;
                updateActivityState();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private boolean checkFields() {
        // checks if fields are valid and in case it returns the error
        boolean result = true;
        AuthorAdapter adapter = (AuthorAdapter) mAuthorsListView.getAdapter();
        boolean validAuthors = false;
        for (String author : adapter.getAuthors()) {
            if (author != null && author.length() > 0) {
                validAuthors = true;
                break;
            }
        }
        if (!validAuthors) {
            Toast.makeText(getApplicationContext(),
                    R.string.insert_author,
                    Toast.LENGTH_SHORT).show();
            result = false;
        } else if (mPublishYearSpinner.getSelectedItemPosition() <= 0) {
            Toast.makeText(getApplicationContext(),
                    R.string.select_publish_year,
                    Toast.LENGTH_SHORT).show();
            result = false;
        } else if (mPublisherTextInputLayout == null ||
                mPublisherTextInputLayout.getEditText() == null ||
                mPublisherTextInputLayout.getEditText().getText() == null ||
                mPublisherTextInputLayout.getEditText().getText().length() <= 0) {
            Toast.makeText(getApplicationContext(),
                    R.string.insert_publisher,
                    Toast.LENGTH_SHORT).show();
            result = false;
        } else if (mConditionsSpinner.getSelectedItemPosition() <= 0) {
            Toast.makeText(getApplicationContext(),
                    R.string.select_condition,
                    Toast.LENGTH_SHORT).show();
            result = false;
        } else if (mGenreSpinner.getSelectedItemPosition() <= 0) {
            Toast.makeText(getApplicationContext(),
                    R.string.select_genre,
                    Toast.LENGTH_SHORT).show();
            result = false;
        } else if (mPositionTextView.getText() == null ||
                mPositionTextView.getText().length() <= 0 ||
                mResultBook.getGeoPoint() == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.insert_position,
                    Toast.LENGTH_SHORT).show();
            result = false;
        } else if (mResultBook.getWebThumbnail() == null &&
                mPhotosRecyclerView.getAdapter().getItemCount() <= 0) {
            Toast.makeText(getApplicationContext(),
                    R.string.insert_photo,
                    Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    private void cleanPhotos() {
        if (mPhotosPath != null) {
            for (String path : mPhotosPath) {
                removePhotoPath(path);
            }
        }
    }

    private void setupToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUploading) {
                    if (mActivityState.equals(ISBN_MANUALLY) || mActivityState.equals(ISBN_ACQUIRED)) {
                        if (mActivityState.equals(ISBN_ACQUIRED))
                            cleanPhotos();
                        mActivityState = ISBN_CHOICE;
                        updateActivityState();
                    } else if (mActivityState.equals(EDIT_BOOK)) {
                        // return where you were
                        finish();
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            }
        });
    }

    private void uploadBookInfo() {
        if (mBeforeEditBook == null) {
            mResultBook.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final DocumentReference newRef = db.collection("books").document();
            mResultBook.setBookID(newRef.getId());
            newRef.set(mResultBook).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //  clean photos
                    if (mPhotosPath != null)
                        for (String path : mPhotosPath) {
                            if (!path.contains("firebasestorage"))
                                removePhotoPath(path);
                        }
                    uploadOnAlgolia(mResultBook, newRef.getId(), true);
                    Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    setResult(RESULT_OK);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    setUploading(false);
                    cleanUpload();
                    finish();
                }
            });
        } else {
            // editing
            final DocumentReference documentReference = FirebaseFirestore.getInstance()
                    .collection("books").document(mBeforeEditBook.getBookID());
            // now join again photos urls which are not marked as to be removed
            for (int i = mBeforeEditBook.getUserBookPhotosStoragePath().size() - 1; i >= 0; i--) {
                mResultBook.getUserBookPhotosStoragePath().add(0, mBeforeEditBook.getUserBookPhotosStoragePath()
                        .get(i));
            }
            FirebaseFirestore.getInstance()
                    .runTransaction(new Transaction.Function<Boolean>() {
                        @Override
                        public Boolean apply(Transaction transaction) throws FirebaseFirestoreException {
                            // read operation for locking document
                            transaction.get(documentReference);
                            transaction.set(documentReference, mResultBook);
                            return true;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    // if there are photo to delete from the storage delete them
                    uploadOnAlgolia(mResultBook, mResultBook.getBookID(), true);
                    if (mMarkedToBeRemoved != null)
                        for (String storageUrl : mMarkedToBeRemoved) {
                            LoadBookActivity.this.removePhotoPath(storageUrl);
                        }

                    Intent i = new Intent(LoadBookActivity.this, HomePageActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (!Utilities.isOnline(getApplicationContext()))
                                Toast.makeText(LoadBookActivity.this,
                                        R.string.no_internet_connection,
                                        Toast.LENGTH_SHORT).show();
                            LoadBookActivity.this.setUploading(false);
                            for (String s : mResultBook.getUserBookPhotosStoragePath()) {
                                if (!mBeforeEditBook.getUserBookPhotosStoragePath()
                                        .contains(s))
                                    // remove new photos
                                    FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(s).delete();
                            }
                            finish();
                        }
                    });

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_photos_text_view:
                final CharSequence[] items = {getString(R.string.camera_option_dialog), getString(R.string.gallery_option_dialog)};
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoadBookActivity.this);
                alertDialogBuilder.setNegativeButton(R.string.negative_button_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                alertDialogBuilder.setTitle(R.string.title_dialog).setItems(items, new DialogInterface.OnClickListener() {
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
                                if (Utilities.checkPermissionActivity(LoadBookActivity.this,
                                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    galleryIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    galleryIntent.setType("image/*");
                                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                                } else {
                                    Utilities.askPermissionActivity(LoadBookActivity.this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Constants.MEMORY_PERMISSION);
                                }
                                break;
                            default:
                                dialogInterface.cancel();
                        }
                    }
                }).show();
                break;
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
        mConditionsSpinner.setAdapter(Utilities.
                makeDropDownAdapter(this, Condition.getConditions(this).toArray(new String[0])));
        mConditionsSpinner.setFloatingLabelText(R.string.conditions);

        mGenreSpinner.setHint(R.string.genre_hint);
        mGenreSpinner.setAdapter(Utilities.makeDropDownAdapter(this,
                getResources().getStringArray(R.array.genre)));
        mGenreSpinner.setFloatingLabelText(R.string.genre_hint);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
            case Constants.MEMORY_PERMISSION:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;

        }
    }

    public void removePhotoPath(String path) {
        if (!path.contains("content")) {
            File file = new File(path);
            boolean delete = file.delete();
            if (file.exists() && !delete) {
                try {
                    delete = file.getCanonicalFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (file.exists() && !delete) {
                    deleteFile(file.getName());
                }
            }
        } else if (path.contains("firebasestorage")) {
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(path)
                    .delete();
        }
    }

    private void setUploading(boolean uploading) {
        mUploading = uploading;
        mProgressBar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        mConditionsSpinner.setEnabled(!uploading);
        mGenreSpinner.setEnabled(!uploading);
        mPositionTextView.setClickable(!uploading);
        mAddPhotosTextView.setClickable(!uploading);
        mTagsTextInputLayout.getEditText().setEnabled(!uploading);
    }

    private void prepareAndStartUpload() {
        setUploading(true);
        // get photos
        if (mPhotosPath != null && !mPhotosPath.isEmpty()) {
            mResultBook.setUserBookPhotosStoragePath(new ArrayList<>(mPhotosPath));
        }
        mResultBook.setCondition(mConditionsSpinner.getSelectedItemPosition() - 1);
        mResultBook.setGenre(mGenreSpinner.getSelectedItemPosition() - 1);
        if (mTags != null) {
            ArrayList<String> validTags = new ArrayList<>();
            for (int i = 0; i < mTags.size(); i++) {
                if (!mTags.get(i).equals(""))
                    validTags.add(mTags.get(i));
            }
            mResultBook.setTags(TextUtils.join(" ", validTags));
        }
        AuthorAdapter authorAdapter = (AuthorAdapter) mAuthorsListView.getAdapter();
        if (authorAdapter != null && authorAdapter.isEditable() && authorAdapter.getAuthors() != null &&
                !authorAdapter.getAuthors().isEmpty()) {
            for (String s : authorAdapter.getAuthors()) {
                mResultBook.addAuthor(s);
            }
        }
        if (mPublishYearSpinner != null
                && mPublishYearSpinner.getSelectedItemPosition() > 0)
            mResultBook.setPublishYear(Integer.parseInt(mPublishYearSpinner.getSelectedItem().toString()));

        if (mResultBook.getPublisher() == null
                && mPublisherTextInputLayout.getEditText() != null)
            mResultBook.setPublisher(mPublisherTextInputLayout.getEditText().getText().toString());

        boolean upload = true;
        // editing a book, check if there are difference among any field
        if (mBeforeEditBook != null) {
            if (!mResultBook.getAuthors().equals(mBeforeEditBook.getAuthors()) ||
                    !mResultBook.getPublisher().equals(mBeforeEditBook.getPublisher()) ||
                    !mResultBook.getPublishYear().equals(mBeforeEditBook.getPublishYear()) ||
                    !mResultBook.getCondition().equals(mBeforeEditBook.getCondition()) ||
                    !mResultBook.getGeoPoint().equals(mBeforeEditBook.getGeoPoint()) ||
                    (mResultBook.getTags() != null && !mResultBook.getTags().equals(mBeforeEditBook.getTags())) ||
                    (mResultBook.getUserBookPhotosStoragePath() != null &&
                            !mResultBook.getUserBookPhotosStoragePath().equals(mBeforeEditBook.getUserBookPhotosStoragePath()))) {
                // there has been a change
                // identify photos to be removed
                if (mBeforeEditBook.getUserBookPhotosStoragePath() != null) {
                    mMarkedToBeRemoved = new ArrayList<>();
                    for (String storagePath : mBeforeEditBook.getUserBookPhotosStoragePath()) {
                        if (!mResultBook.getUserBookPhotosStoragePath()
                                .contains(storagePath)) {
                            mMarkedToBeRemoved.add(storagePath);
                        } else
                            mResultBook.getUserBookPhotosStoragePath().remove(storagePath);
                    }
                    if (!mMarkedToBeRemoved.isEmpty())
                        mBeforeEditBook.getUserBookPhotosStoragePath().removeAll(mMarkedToBeRemoved);
                }

            } else {
                upload = false;
            }
        }


        if (mResultBook.getUserBookPhotosStoragePath() != null
                && !mResultBook.getUserBookPhotosStoragePath().isEmpty()
                && upload) {
            mProgressRate = 100 / mResultBook.getUserBookPhotosStoragePath().size();
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(0);
            mUploadedImagesCount = 0;
            uploadPhotos();
        } else if (upload) {
            mProgressBar.setIndeterminate(true);
            mResultBook.nullifyInvalidStrings(); // clean "" fields
            uploadBookInfo();
        } else {
            finish();
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

    private void uploadPhotos() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        if (mResultBook.getUserBookPhotosStoragePath().size() > 0) {
            StorageReference storageRef = storageReference.child(generateStorageRef(mResultBook.getUserBookPhotosStoragePath()
                    .get(mUploadedImagesCount)));
            byte[] compressedImage = null;
            compressedImage = Utilities.compressPhoto(mResultBook.getUserBookPhotosStoragePath()
                            .get(mUploadedImagesCount),
                    getContentResolver(),
                    getApplicationContext());

            if (compressedImage == null) {
                Toast.makeText(getApplicationContext(),
                        R.string.load_book_upload_error,
                        Toast.LENGTH_SHORT).show();
                cleanUpload();
                finish();
            }
            UploadTask uploadTask = storageRef.putBytes(compressedImage);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressBar.setProgress(mProgressRate * (mUploadedImagesCount + 1));
                    LoadBookActivity.this.removePhotoPath(mResultBook.getUserBookPhotosStoragePath()
                            .get(mUploadedImagesCount));
                    mResultBook.getUserBookPhotosStoragePath().set(mUploadedImagesCount, taskSnapshot.getDownloadUrl().toString());
                    if (++mUploadedImagesCount >= mResultBook.getUserBookPhotosStoragePath().size()) {
                        // upload ended move uploading book info
                        mProgressBar.setIndeterminate(true);
                        uploadBookInfo();
                    } else {
                        uploadPhotos();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    setUploading(false);
                    cleanUpload();
                    Toast.makeText(getApplicationContext(),
                            R.string.load_book_upload_error,
                            Toast.LENGTH_LONG)
                            .show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }
    }

    private void cleanUpload() {
        for (int i = mUploadedImagesCount; i >= 0; i--) {
            removePhotoPath(mResultBook.getUserBookPhotosStoragePath()
                    .get(i));
        }
    }

    private void setupAlgolia() {
        algoliaClient = new Client(ALGOLIA_APP_ID, ALGOLIA_API_KEY);
        algoliaIndex = algoliaClient.getIndex(ALGOLIA_BOOK_INDEX);
    }

    private void uploadOnAlgolia(Book bookToLoad, String bookID, boolean update) {
        try {
            JSONObject book = new JSONObject();

            book.put("title", bookToLoad.getTitle());
            book.put("author", bookToLoad.getAuthors());
            book.put("publisher", bookToLoad.getPublisher());
            book.put("publishYear", bookToLoad.getPublishYear());
            book.put("conditions", bookToLoad.getCondition());
            book.put("uid", bookToLoad.getUid());
            book.put("genre", bookToLoad.getGenre());

            JSONObject coordinates = new JSONObject();
            coordinates.put("lat", bookToLoad.getGeoPoint().getLatitude());
            coordinates.put("lng", bookToLoad.getGeoPoint().getLongitude());
            book.put("_geoloc", coordinates);
            book.put("timestamp", bookToLoad.getTimeInserted());

            if (bookToLoad.getWebThumbnail() != null) {
                book.put("thumbnail", bookToLoad.getWebThumbnail());
            } else if (bookToLoad.getUserBookPhotosStoragePath().size() > 0) {
                book.put("thumbnail", bookToLoad.getUserBookPhotosStoragePath().get(0));
            }
            if (bookToLoad.getTags() != null) {
                book.put("tags", bookToLoad.getTags());
            }

            if (update)
                algoliaIndex.saveObjectAsync(book, bookID, null);
            else
                algoliaIndex.addObjectAsync(book, bookID, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
