package com.example.android.lab1.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import static com.example.android.lab1.utils.Constants.CAPTURE_IMAGE;
import static com.example.android.lab1.utils.Constants.RESULT_LOAD_IMAGE;
import static com.example.android.lab1.utils.Constants.SCAN_REQUEST_TAG;

public class LoadBookActivity2 extends AppCompatActivity implements View.OnClickListener, RemovePhotoClickListener {

    private Toolbar mToolbar;
    private MaterialSpinner mPublishYearSpinner;
    private MaterialSpinner mConditionsSpinner;
    private MaterialSpinner mGenreSpinner;
    private EditText mIsbnEditText;
    private ImageView mSearchOnInternetImageView;
    private NestedScrollView mInfoContainerScrollView;
    private ImageView mBookWebThumbnailImageView;
    private TextInputLayout mTitleTextInputLayout;
    private LinearLayoutCompat mAuthorsListView;
    private TextInputLayout mPublisherTextInputLayout;
    private TextView mPositionTextView;
    private TextView mLoanRangeTextView;
    private AppCompatButton mScanBarcodeButton;
    private ImageView mIsbnExplanationImageView;
    private RequestQueue mRequestQueue;
    private ImageButton mAddAuthorButton;
    private ImageButton mRemoveAuthorButton;
    private TextView mConfirmedIsbn;
    private ProgressBar mProgressBar;
    private RecyclerView mPhotosGrid;
    private ImageButton mAddPhotos;
    private Book result;
    private AlertDialog.Builder mAlertDialogBuilder;
    private File mPhotoFile;
    private Activity mSelf;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book_2);
        initializeWidgets();

        isbnInserted(savedInstanceState != null && savedInstanceState.containsKey("ISBN_SCANNED"));

        setupToolBar();
        setupSpinners();
        setupIsbnListeners();
        setupListeners();
        setupGallery();
    }

    private void setupGallery() {
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(this);
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPhotosGrid.setAdapter(new PhotosAdapter(this));
        mPhotosGrid.setLayoutManager(MyLayoutManager);
    }

    private void isbnInserted(boolean isbnScanned) {
        if (isbnScanned) {
            mProgressBar.setVisibility(View.GONE);
            mSearchOnInternetImageView.setVisibility(View.GONE);
            mScanBarcodeButton.setVisibility(View.GONE);
            findViewById(R.id.load_book_isbn).setVisibility(View.GONE);
            mIsbnEditText.setVisibility(View.GONE);

            mIsbnExplanationImageView.setVisibility(View.GONE);
            mInfoContainerScrollView.setVisibility(View.VISIBLE);
        } else {
            mSearchOnInternetImageView.setVisibility(View.VISIBLE);
            mScanBarcodeButton.setVisibility(View.VISIBLE);
            findViewById(R.id.load_book_isbn).setVisibility(View.VISIBLE);
            mIsbnEditText.setVisibility(View.VISIBLE);

            mIsbnExplanationImageView.setVisibility(View.VISIBLE);
            mInfoContainerScrollView.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.SCAN_REQUEST_TAG:
                if (resultCode == Activity.RESULT_OK) {
                    // Parsing bar code reader result
                    if (data != null && data.hasExtra(ScanBarCodeActivity.BARCODE_KEY)) {
                        if (!mIsbnEditText.getText().toString().equals(data.getStringExtra(ScanBarCodeActivity.BARCODE_KEY))) {
                            mIsbnEditText.setText(data.getStringExtra(ScanBarCodeActivity.BARCODE_KEY));
                            makeRequestBookApi(mIsbnEditText.getText().toString(),
                                    false);
                        }
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

        final Activity mSelf = this;
        mScanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.startBarcodeScanner(mSelf);
            }
        });

        mAddPhotos.setOnClickListener(this);

    }

    private void initializeWidgets() {
        TextInputLayout isbnTextContainer = findViewById(R.id.load_book_isbn);
        mIsbnEditText = isbnTextContainer.getEditText();
        mConfirmedIsbn = findViewById(R.id.load_book_confirmed_isbn);
        mIsbnExplanationImageView = findViewById(R.id.load_book_isbn_explanation);
        mSearchOnInternetImageView = findViewById(R.id.load_book_search_internet);
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
        // Autofill icon
        mSearchOnInternetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsbnEditText != null &&
                        mIsbnEditText.getText() != null &&
                        mIsbnEditText.getText().length() > 0) {
                    mTitleTextInputLayout.getEditText().clearFocus();
                    mPublishYearSpinner.clearFocus();
                    mPublishYearSpinner.clearFocus();
                    makeRequestBookApi(mIsbnEditText.getText().toString(), false);
                }
            }
        });

        // Scanner icon
        final Activity thisActivity = this;
        mScanBarcodeButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                                              Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                          Utilities.startBarcodeScanner(thisActivity);
                                                      } else {
                                                          ActivityCompat.requestPermissions(thisActivity,
                                                                  new String[]{Manifest.permission.CAMERA},
                                                                  SCAN_REQUEST_TAG);
                                                      }
                                                  }
                                              }
        );
    }


    private void makeRequestBookApi(final String isbn, final boolean alternativeEndPoint) {
        mSearchOnInternetImageView.setVisibility(View.GONE);
        final ProgressBar mProgressBar = findViewById(R.id.load_book_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);

        String url;
        String readIsbn = isbn.replace("-", "");
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
                        JSONObject book = response.getJSONArray("items")
                                .getJSONObject(0).getJSONObject("volumeInfo");

                        mConfirmedIsbn.setText("ISBN:" + mIsbnEditText.getText());

                        mTitleTextInputLayout.getEditText().setText(book.optString("title"));
                        if (mTitleTextInputLayout.getEditText().getText() != null &&
                                mTitleTextInputLayout.getEditText().getText().length() > 0)
                            mTitleTextInputLayout.getEditText().setEnabled(false);
                        /*if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0)
                            mAuthorEditText.getEditText().setText(book.getJSONArray("authors")
                                    .getString(0));
                        else
                            mAuthorEditText.getEditText().setText("");*/
                        mPublisherTextInputLayout.getEditText().setText(book.optString("publisher"));
                        if (mPublisherTextInputLayout.getEditText().getText() != null &&
                                mPublisherTextInputLayout.getEditText().getText().length() > 0)
                            mPublisherTextInputLayout.getEditText().setEnabled(false);

                        if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0) {
                            JSONArray authors = book.getJSONArray("authors");
                            int numOfViews = authors.length() > 5 ? 5 : authors.length();
                            int actualViews = getAuthorsViewsCount();

                            for (int i = 0; i < numOfViews; i++) {
                                if (i < actualViews)
                                    setAuthorViewText(authors.getString(i), i);
                                else
                                    createAuthorView(authors.getString(i));
                            }
                            mAddAuthorButton.setVisibility(View.GONE);
                            mRemoveAuthorButton.setVisibility(View.GONE);
                        }

                        if (book.has("publishedDate")) {
                            int currentYear, publishYear;
                            currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            publishYear = book.getInt("publishedDate");
                            mPublishYearSpinner.setSelection(currentYear - publishYear);
                            mPublishYearSpinner.setEnabled(false);
                        }

                        if (book.has("imageLinks")) {
                            JSONObject imageLinks = book.getJSONObject("imageLinks");
                            if (imageLinks.length() > 0 &&
                                    imageLinks.has("thumbnail")) {
                                Glide.with(getApplicationContext())
                                        .load(imageLinks.getString("thumbnail"))
                                        .into(mBookWebThumbnailImageView);
                            }
                        }

                        /*animateFade(mProgressBar, true);
                        animateFade(mSearchOnInternetImageView, true);
                        animateFade(mScanBarcodeButton, true);
                        animateFade(mIsbnExplanationImageView, true);

                        animateFade(mInfoContainerScrollView, false);*/

                        //AnimatorSet animatorSet = new AnimatorSet();


                        isbnInserted(true);

                        mTitleTextInputLayout.getEditText().clearFocus();
                        mPublishYearSpinner.clearFocus();
                        mPublishYearSpinner.clearFocus();


                    } else {
                        // second request
                        if (alternativeEndPoint)
                            showSnackbar(R.string.load_book_book_not_found);
                        else
                            makeRequestBookApi(isbn, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressBar.setVisibility(View.GONE);
                mSearchOnInternetImageView.setVisibility(View.VISIBLE);
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
        adapter.removeItem(position);
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
}
