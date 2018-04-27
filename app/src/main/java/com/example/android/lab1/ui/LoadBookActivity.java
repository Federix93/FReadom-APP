package com.example.android.lab1.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.lab1.adapter.ImagePagerAdapter;
import com.example.android.lab1.R;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.Condition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ganfra.materialspinner.MaterialSpinner;

public class LoadBookActivity extends AppCompatActivity implements View.OnClickListener, OnSuccessListener<UploadTask.TaskSnapshot>, OnFailureListener {

    private static final String ISBN_REQUEST_TAG = "ISBN_REQUEST";
    private static final int SCAN_REQUEST_TAG = 3;
    private static final int POSITION_REQUEST = 2;
    private static final String NUM_OF_DOTS = "NUM_OF_DOTS";
    private static final String SELECTED_DOT = "SELECTED_DOT";
    private static final String ISBN_FIELD = "ISBN";
    private static final String TITLE_FIELD = "TITLE";
    private static final String AUTHOR_FIELD = "AUTHOR";
    private static final String PUBLISHER_FIELD = "PUBLISHER";
    private static final String POSITION_FIELD = "POSITION";
    private static final String TAGS_FIELD = "TAGS";
    private static final String WEB_THUMBNAIL_FIELD = "WEB_THUMBNAIL";
    private static final String PHOTOS_PATH_FIELD = "PHOTOS_PATH";
    private static final String CURRENT_PHOTO = "CURRENT_PHOTO";
    private static final String CURRENT_STORAGE_REF = "CURRENT_STORAGE_REF";
    private static final String IMAGE_DOWNLOAD_URLS = "IMAGE_DOWNLOAD_URLS";
    private static final String UPLOADED_IMAGE_COUNT = "UPLOADED_IMAGES_COUNT";
    private static final String UPLOADING = "UPLOADING";
    private static final String IMAGES_UPLOADED = "IMAGES_UPLOADED";
    private static final String DOCUMENT_UPLOADED = "DOCUMENT_UPLOADED";
    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;
    protected FirebaseAuth mFirebaseAuth;
    private ViewPager mGallery;
    private ImageView mAddImageFAB;
    private TextInputLayout mIsbnEditText;
    private ImageView mIsbnScanBarCode;
    private ImageView mIsbnImageView;
    private TextInputLayout mTitleEditText;
    private TextInputLayout mAuthorEditText;
    private TextInputLayout mPublisherEditText;
    private MaterialSpinner mPublishYearSpinner;
    private MaterialSpinner mConditionsSpinner;
    private TextInputLayout mTagsEditText;
    private TextView mPositionEditText;
    private ImageView mPositionIcon;
    private Toolbar mToolbar;
    private ImageView mConfirmImageView;
    private AlertDialog.Builder mAlertDialogBuilder;
    private File mPhotoFile; // last photo thumbnail
    private LinearLayout mDotsContainer;
    private long lastClickTime = 0;
    private ArrayList<String> mPhotosPath;
    private String mWebThumbnail;
    private RequestQueue mRequestQueue;
    private StorageReference mStorageRef;
    private Integer mUploadedImagesCount;
    private ArrayList<String> mDownloadUrls;
    private boolean mUploading;
    private boolean mImagesUploaded;
    private boolean mDocumentUploaded;

    public static String getSha1Hex(String clearString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book);

        mGallery = findViewById(R.id.load_book_gallery);
        mAddImageFAB = findViewById(R.id.add_image_fab);
        mIsbnEditText = findViewById(R.id.load_book_isbn);
        mIsbnScanBarCode = findViewById(R.id.load_book_scan_bar_code);
        mIsbnImageView = findViewById(R.id.load_book_auto_fill);
        mTitleEditText = findViewById(R.id.load_book_title);
        mAuthorEditText = findViewById(R.id.load_book_author);
        mPublisherEditText = findViewById(R.id.load_book_publisher);
        mPublishYearSpinner = findViewById(R.id.load_book_publish_year_spinner);
        mConditionsSpinner = findViewById(R.id.load_book_conditions_spinner);
        mPositionEditText = findViewById(R.id.load_book_position);
        mTagsEditText = findViewById(R.id.load_book_tags);
        mPositionIcon = findViewById(R.id.load_book_location);
        mToolbar = findViewById(R.id.toolbar_loadbook);
        mConfirmImageView = findViewById(R.id.icon_check_toolbar);
        mDotsContainer = findViewById(R.id.dots_container);

        // TODO add recieve intent to modify book


        // complex listeners are handled in separate function
        mUploading = false;
        mImagesUploaded = false;
        setupToolBar();
        setupCameraButton();
        setupIsbnListeners();
        setupGalleryListeners();
        setupSpinners();
        // position listener
        mPositionEditText.setOnClickListener(this);
        mPositionIcon.setOnClickListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(PHOTOS_PATH_FIELD))
            mPhotosPath = savedInstanceState.getStringArrayList(PHOTOS_PATH_FIELD);
        else
            mPhotosPath = new ArrayList<>();

        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    private void setupCameraButton() {
        final Activity thisActivity = this;
        mAddImageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final CharSequence[] items = {getString(R.string.camera_option_dialog), getString(R.string.gallery_option_dialog)};
                mAlertDialogBuilder = new AlertDialog.Builder(view.getContext());
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
                            case CAPTURE_IMAGE:
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
                                    if (!Utilities.checkPermissionActivity(thisActivity, Manifest.permission.CAMERA)) {
                                        Utilities.askPermissionActivity(thisActivity, Manifest.permission.CAMERA, CAPTURE_IMAGE);
                                    } else {
                                        startActivityForResult(cameraIntent, CAPTURE_IMAGE);
                                    }

                                }
                                break;
                            case RESULT_LOAD_IMAGE:
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                galleryIntent.setType("image/*");
                                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                                break;
                            default:
                                dialogInterface.cancel();
                        }
                    }
                }).show();
            }
        });
    }

    private void setupToolBar() {
        // toolbar
        mToolbar.setTitle(R.string.load_book_toolbar_title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete photos
                for (String s : mPhotosPath) {
                    File f = new File(s);
                    if (f.isFile())
                        f.delete();
                }
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mToolbar.inflateMenu(R.menu.add_book);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
                }
                return true;

            }
        });
    }

    private byte[] compressPhoto(String filePath) {
        Uri filePathUri = Uri.parse(mPhotosPath.get(mUploadedImagesCount));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        ByteArrayOutputStream out;
        Bitmap bitmap = null;
        if (filePathUri.getScheme() != null && filePathUri.getScheme().equals("content")) {
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(filePathUri));
            } catch (FileNotFoundException e) {
                return null;
            }
        } else
            bitmap = BitmapFactory.decodeFile(filePath, options);
        // rotate bitmap
        Matrix m = new Matrix();
        m.postRotate(90);
        Bitmap compressedRotated = Bitmap.createBitmap(bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                m,
                true);
        out = new ByteArrayOutputStream();
        compressedRotated.compress(Bitmap.CompressFormat.JPEG, 75, out);
        return out.toByteArray();
    }

    private String generateStorageRef(String path) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String lastPathSegment = Uri.fromFile(new File(path)).getLastPathSegment();
        String lastPathHash = getSha1Hex(lastPathSegment);

        return uid + "/images/books/" + lastPathHash + "-" + lastPathSegment + ".jpg";
    }

    private void uploadPhotos() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        mUploadedImagesCount = 0;
        if (mPhotosPath.size() > 0) {
            mDownloadUrls = new ArrayList<>();
            mStorageRef = storageReference.child(generateStorageRef(mPhotosPath.get(mUploadedImagesCount)));

            byte[] compressedImage = compressPhoto(mPhotosPath.get(mUploadedImagesCount));
            if (compressedImage == null) {
                Toast.makeText(getApplicationContext(),
                        R.string.load_book_upload_error,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            UploadTask uploadTask = mStorageRef.putBytes(compressedImage);
            uploadTask.addOnSuccessListener(this, this).addOnFailureListener(this, this);
        }
    }

    private void uploadBookInfo() {
        Integer condition = null;
        if(mConditionsSpinner.getSelectedItem() != null){
            condition = mConditionsSpinner.getSelectedItemPosition();
        }


        final Book bookToLoad = new Book();
        bookToLoad.setIsbn(mIsbnEditText.getEditText().getText().toString());
        bookToLoad.setTitle(mTitleEditText.getEditText().getText().toString());
        bookToLoad.setAuthor(mAuthorEditText.getEditText().getText().toString());
        bookToLoad.setPublisher(mPublisherEditText.getEditText().getText().toString());
        bookToLoad.setPublishYear(Integer.parseInt(mPublishYearSpinner.getSelectedItem().toString()));
        bookToLoad.setConditions(condition);
        bookToLoad.setAddress(mPositionEditText.getText().toString());
        bookToLoad.setTags(mTagsEditText.getEditText().getText().toString());
        bookToLoad.setUid(mFirebaseAuth.getCurrentUser().getUid());

        if (mWebThumbnail != null)
            bookToLoad.setThumbnail(mWebThumbnail);
        if (mDownloadUrls != null)
            bookToLoad.setBookImagesUrls(mDownloadUrls);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mDocumentUploaded = true;
        db.collection("books").add(bookToLoad).addOnSuccessListener(this, new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // using documentReference create a folder on storage for storing photos
                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                findViewById(R.id.load_book_progress_bar).setVisibility(View.GONE);
                mIsbnImageView.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), R.string.load_book_upload_error, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void lockUI(boolean doIt) {
        // this methods disable all UI controls while downloads or uploads are happening
        doIt = !doIt;
        mIsbnEditText.setEnabled(doIt);
        mTitleEditText.setEnabled(doIt);
        mAuthorEditText.setEnabled(doIt);
        mPublisherEditText.setEnabled(doIt);
        mIsbnScanBarCode.setClickable(doIt);
        mIsbnImageView.setClickable(doIt);
        mAddImageFAB.setClickable(doIt);
        mConditionsSpinner.setEnabled(doIt);
        mPublishYearSpinner.setEnabled(doIt);
        mPositionEditText.setClickable(doIt);
        mPositionIcon.setClickable(doIt);
    }


    private void setupSpinners() {
        String years[] = new String[200];

        for (int i = Calendar.getInstance().get(Calendar.YEAR), n = 0; n < 200; i--, n++)
            years[n] = Integer.toString(i);

        mPublishYearSpinner.setAdapter(makeDropDownAdapter(years));
        mPublishYearSpinner.setHint(R.string.publishing_year);
        mPublishYearSpinner.setFloatingLabelText(R.string.publishing_year);

        mConditionsSpinner.setHint(R.string.conditions);
        mConditionsSpinner.setAdapter(makeDropDownAdapter(Condition.getConditions(this).toArray(new String[0])));
        mConditionsSpinner.setFloatingLabelText(R.string.conditions);
    }

    private void setupGalleryListeners() {
        mGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TextView dot;
                for (int i = 0; i < mDotsContainer.getChildCount(); i++) {
                    dot = (TextView) mDotsContainer.getChildAt(i);
                    if (i == position)
                        dot.setTextColor(Color.WHITE);
                    else
                        dot.setTextColor(Color.GRAY);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mGallery.addOnAdapterChangeListener(new ViewPager.OnAdapterChangeListener() {
            @Override
            public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
                if (mDotsContainer != null && mGallery.getAdapter() != null && mDotsContainer.getChildCount() != mGallery.getAdapter().getCount()) {
                    TextView newDot = new TextView(getApplicationContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER;
                    newDot.setText(".");
                    newDot.setTextSize(45);
                    newDot.setTypeface(null, Typeface.BOLD);
                    newDot.setLayoutParams(params);
                    if (mDotsContainer.getChildCount() == 0)
                        newDot.setTextColor(Color.WHITE);
                    else
                        newDot.setTextColor(Color.GRAY);
                    mDotsContainer.addView(newDot);

                }
            }
        });
    }

    private void makeRequestBookApi(final String isbn, final boolean alternativeEndPoint) {
        mIsbnImageView.setVisibility(View.GONE);
        final ProgressBar mProgressBar = findViewById(R.id.load_book_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

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
                mProgressBar.setVisibility(View.GONE);
                mIsbnImageView.setVisibility(View.VISIBLE);
                try {
                    if (response.getInt("totalItems") > 0) {
                        JSONObject book = response.getJSONArray("items")
                                .getJSONObject(0).getJSONObject("volumeInfo");

                        mTitleEditText.getEditText().setText(book.optString("title"));
                        if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0)
                            mAuthorEditText.getEditText().setText(book.getJSONArray("authors")
                                    .getString(0));
                        else
                            mAuthorEditText.getEditText().setText("");
                        mPublisherEditText.getEditText().setText(book.optString("publisher"));

                        if (book.has("publishedDate")) {
                            int currentYear, publishYear;
                            currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            publishYear = book.getInt("publishedDate");
                            mPublishYearSpinner.setSelection(currentYear - publishYear);
                        }

                        if (book.has("imageLinks")) {
                            JSONObject imageLinks = book.getJSONObject("imageLinks");
                            if (imageLinks.length() > 0 &&
                                    imageLinks.has("thumbnail")) {
                                mWebThumbnail = imageLinks.getString("thumbnail");
                            }
                        }

                        mTitleEditText.clearFocus();
                        mAuthorEditText.clearFocus();
                        mPublishYearSpinner.clearFocus();
                    } else {
                        // second request
                        if (alternativeEndPoint)
                            Toast.makeText(getApplicationContext(),
                                    R.string.load_book_book_not_found,
                                    Toast.LENGTH_SHORT).show();
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
                mIsbnImageView.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), R.string.load_book_autofill_request_error, Toast.LENGTH_SHORT).show();
            }
        });
        jsonRequest.setTag(ISBN_REQUEST_TAG);
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(jsonRequest);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(ISBN_REQUEST_TAG);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mIsbnEditText != null)
            outState.putString(ISBN_FIELD, mIsbnEditText.getEditText().getText().toString());
        if (mTitleEditText != null)
            outState.putString(TITLE_FIELD, mTitleEditText.getEditText().getText().toString());
        if (mAuthorEditText != null)
            outState.putString(AUTHOR_FIELD, mAuthorEditText.getEditText().getText().toString());
        if (mPublisherEditText != null)
            outState.putString(PUBLISHER_FIELD, mPublisherEditText.getEditText().getText().toString());
        if (mPositionEditText != null && mPositionEditText.getText() != null)
            outState.putString(POSITION_FIELD, mPositionEditText.getText().toString());
        if(mTagsEditText != null)
            outState.putString(TAGS_FIELD, mTagsEditText.getEditText().getText().toString());
        if (mWebThumbnail != null)
            outState.putString(WEB_THUMBNAIL_FIELD, mWebThumbnail);
        if (mPhotosPath != null)
            outState.putStringArrayList(PHOTOS_PATH_FIELD, mPhotosPath);
        if (mGallery != null && mDotsContainer != null && mGallery.getAdapter() != null) {
            outState.putInt(NUM_OF_DOTS, mGallery.getAdapter().getCount());
            outState.putInt(SELECTED_DOT, mGallery.getCurrentItem());
        }
        if (mPhotoFile != null) {
            outState.putString(CURRENT_PHOTO, mPhotoFile.getAbsolutePath());
        }
        if (mStorageRef != null) {
            outState.putString(CURRENT_STORAGE_REF, mStorageRef.toString());
        }
        if (mDownloadUrls != null) {
            outState.putStringArrayList(IMAGE_DOWNLOAD_URLS, mDownloadUrls);
        }
        if (mUploadedImagesCount != null) {
            outState.putInt(UPLOADED_IMAGE_COUNT, mUploadedImagesCount);
        }
        outState.putBoolean(IMAGES_UPLOADED, mImagesUploaded);
        outState.putBoolean(DOCUMENT_UPLOADED, mDocumentUploaded);
        outState.putBoolean(UPLOADING, mUploading);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(ISBN_FIELD))
            mIsbnEditText.getEditText().setText(savedInstanceState.getString(ISBN_FIELD));
        if (savedInstanceState.containsKey(TITLE_FIELD))
            mTitleEditText.getEditText().setText(savedInstanceState.getString(TITLE_FIELD));
        if (savedInstanceState.containsKey(AUTHOR_FIELD))
            mAuthorEditText.getEditText().setText(savedInstanceState.getString(AUTHOR_FIELD));
        if (savedInstanceState.containsKey(PUBLISHER_FIELD))
            mPublisherEditText.getEditText().setText(savedInstanceState.getString(PUBLISHER_FIELD));
        if (savedInstanceState.containsKey(POSITION_FIELD))
            mPositionEditText.setText(savedInstanceState.getString(POSITION_FIELD));
        if(savedInstanceState.containsKey(TAGS_FIELD))
            mTagsEditText.getEditText().setText(savedInstanceState.getString(TAGS_FIELD));
        if (savedInstanceState.containsKey(WEB_THUMBNAIL_FIELD))
            mWebThumbnail = savedInstanceState.getString(WEB_THUMBNAIL_FIELD);
        if (savedInstanceState.containsKey(PHOTOS_PATH_FIELD)) {
            mPhotosPath = savedInstanceState.getStringArrayList(PHOTOS_PATH_FIELD);
            mGallery.setAdapter(new ImagePagerAdapter(getApplicationContext(), mPhotosPath, (LinearLayout) findViewById(R.id.dots_container)));
        }
        if (savedInstanceState.containsKey(NUM_OF_DOTS))
            buildDotView(savedInstanceState.getInt(NUM_OF_DOTS),
                    savedInstanceState.getInt(SELECTED_DOT));
        if (savedInstanceState.containsKey(CURRENT_PHOTO))
            mPhotoFile = new File(savedInstanceState.getString(CURRENT_PHOTO));
        if (savedInstanceState.containsKey(IMAGE_DOWNLOAD_URLS))
            mDownloadUrls = savedInstanceState.getStringArrayList(IMAGE_DOWNLOAD_URLS);
        if (savedInstanceState.containsKey(UPLOADED_IMAGE_COUNT))
            mUploadedImagesCount = savedInstanceState.getInt(UPLOADED_IMAGE_COUNT);

        if (savedInstanceState.containsKey(UPLOADING)) {
            mUploading = savedInstanceState.getBoolean(UPLOADING);
            if (mUploading) {
                lockUI(true);
                mIsbnImageView.setVisibility(View.GONE);
                final ProgressBar mProgressBar = findViewById(R.id.load_book_progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        if (savedInstanceState.containsKey(CURRENT_STORAGE_REF)) {
            mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(savedInstanceState.getString(CURRENT_STORAGE_REF));
            List<UploadTask> activeDownloads = mStorageRef.getActiveUploadTasks();
            if (activeDownloads.size() > 0) {
                Log.d("DEBUGUPLOAD", "onRestoreInstanceState: Restarted download");
                activeDownloads.get(0).addOnSuccessListener(this)
                        .addOnFailureListener(this)
                        .resume();
            }

        }

        if (savedInstanceState.containsKey(IMAGES_UPLOADED)) {
            mImagesUploaded = savedInstanceState.getBoolean(IMAGES_UPLOADED);
        }
        if (savedInstanceState.containsKey(DOCUMENT_UPLOADED))
            mDocumentUploaded = savedInstanceState.getBoolean(DOCUMENT_UPLOADED);
        if (!mDocumentUploaded && mImagesUploaded)
            uploadBookInfo();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mStorageRef != null) {
            List<UploadTask> activeUploadTasks = mStorageRef.getActiveUploadTasks();
            if (activeUploadTasks.size() > 0) {
                if (activeUploadTasks.get(0).isPaused()) {
                    activeUploadTasks.get(0).addOnSuccessListener(this).addOnFailureListener(this).resume();
                    Log.d("DEBUGUPLOAD", "onResume: resumed upload");
                }
            }
        }
    }

    private ArrayAdapter<String> makeDropDownAdapter(String[] items) {
        List<String> temp;
        temp = new ArrayList<>(Arrays.asList(items));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                temp);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return arrayAdapter;
    }

    private boolean validateIsbn(EditText isbnView) {
        if (isbnView.getText().length() > 0) {
            Pattern isbn = Pattern.compile("(?:\\d-?){13}");
            Matcher matcher = isbn.matcher(isbnView.getText().toString());
            if (!matcher.matches()) {
                String errorMessage = getString(R.string.invalid_isbn);
                isbnView.setError(errorMessage);
                return false;
            } else {
                isbnView.setError(null);
                return true;
            }
        } else {
            isbnView.setError(null);
        }
        return false;
    }

    private void setupIsbnListeners() {
        // ISBN Edit Text
        mIsbnEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && v instanceof EditText) {
                    EditText isbnView = (EditText) v;
                    validateIsbn(isbnView);
                }
            }
        });

        mIsbnEditText.getEditText().addTextChangedListener(new TextWatcher() {
            int mBefore;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mBefore = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > mBefore) {
                    // writing
                    if (s.toString().replace("-", "").length() == 13)
                        validateIsbn(mIsbnEditText.getEditText());
                } else {
                    // deleting
                    validateIsbn(mIsbnEditText.getEditText());
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Autofill icon
        mIsbnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateIsbn(mIsbnEditText.getEditText())) {
                    makeRequestBookApi(mIsbnEditText.getEditText().getText().toString(), false);
                }

            }
        });

        // Scanner icon
        final Activity thisActivity = this;
        mIsbnScanBarCode.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                        startBarCodeScanner();
                                                    } else {
                                                        ActivityCompat.requestPermissions(thisActivity,
                                                                new String[]{Manifest.permission.CAMERA},
                                                                SCAN_REQUEST_TAG);
                                                    }
                                                }
                                            }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case SCAN_REQUEST_TAG:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBarCodeScanner();
                    break;
                }
            case CAPTURE_IMAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                    break;
                }
        }
    }

    private void startCamera() {
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

            startActivityForResult(cameraIntent, CAPTURE_IMAGE);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        switch (requestCode) {
            case SCAN_REQUEST_TAG:
                if (resultCode == Activity.RESULT_OK) {
                    // Parsing bar code reader result
                    if (resultData != null && resultData.hasExtra(ScanBarCodeActivity.BARCODE_KEY)) {
                        if (!mIsbnEditText.getEditText().getText().toString().equals(resultData.getStringExtra(ScanBarCodeActivity.BARCODE_KEY))) {
                            // disable input filters temporarely
                            InputFilter[] filters = mIsbnEditText.getEditText().getFilters();
                            mIsbnEditText.getEditText().setFilters(new InputFilter[]{});
                            mIsbnEditText.getEditText().setText(resultData.getStringExtra(ScanBarCodeActivity.BARCODE_KEY));
                            mIsbnEditText.getEditText().setFilters(filters);
                            validateIsbn(mIsbnEditText.getEditText());
                            mTitleEditText.getEditText().setText("");
                            mAuthorEditText.getEditText().setText("");
                            mPublisherEditText.getEditText().setText("");
                            mPublishYearSpinner.setSelection(0);
                        }
                    }
                }
                break;
            case POSITION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (resultData != null && resultData.hasExtra(PositionActivity.ADDRESS_KEY)) {
                        if (mPositionEditText != null) {
                            mPositionEditText.setText(resultData.getStringExtra(PositionActivity.ADDRESS_KEY));
                        }
                    }
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK && mPhotoFile != null) {
                    updateGallery(mPhotoFile.getAbsolutePath(), null);
                } else if (resultCode == RESULT_CANCELED && mPhotoFile != null) {
                    if (mPhotoFile.isFile())
                        mPhotoFile.delete();
                }
                break;
            case RESULT_LOAD_IMAGE:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    if (resultData.getData() != null) {
                        updateGallery(resultData.getData().toString(), null);
                    }
                }
                break;
        }
    }

    private void startBarCodeScanner() {
        Intent i = new Intent(this, ScanBarCodeActivity.class);
        startActivityForResult(i, SCAN_REQUEST_TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_book_position:
            case R.id.load_book_location:
                Intent intent = new Intent(this, PositionActivity.class);
                startActivityForResult(intent, POSITION_REQUEST);
                break;
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

    private void updateGallery(String newPhoto, Integer optPosition) {
        if (optPosition == null) {
            mPhotosPath.add(newPhoto);
        } else if (optPosition >= 0 && optPosition < mPhotosPath.size())
            mPhotosPath.add(optPosition, newPhoto);
        else
            optPosition = null;
        mGallery.setAdapter(new ImagePagerAdapter(getApplicationContext(), mPhotosPath, (LinearLayout) findViewById(R.id.dots_container)));
        mGallery.setCurrentItem(optPosition == null ? mPhotosPath.size() - 1 : optPosition, true);
    }

    private void buildDotView(int itemsCount, int selectedItem) {
        if (mDotsContainer != null) {
            mDotsContainer.removeAllViews();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            for (int i = 0; i < itemsCount; i++) {
                TextView dot = new TextView(getApplicationContext());
                dot.setTextColor(i == selectedItem ? Color.WHITE : Color.GRAY);
                dot.setText(".");
                dot.setTextSize(45);
                dot.setTypeface(null, Typeface.BOLD);
                dot.setLayoutParams(params);
                mDotsContainer.addView(dot);
            }
        }
    }

    private boolean checkObligatoryFields() {
        return mIsbnEditText != null && mIsbnEditText.getEditText() != null && mIsbnEditText.getEditText().getText() != null && mIsbnEditText.getEditText().getText().length() > 0 && mIsbnEditText.getError() == null &&
                mTitleEditText != null && mTitleEditText.getEditText() != null && mTitleEditText.getEditText().getText() != null && mTitleEditText.getEditText().getText().length() > 0 &&
                mAuthorEditText != null && mAuthorEditText.getEditText() != null && mAuthorEditText.getEditText().getText() != null && mAuthorEditText.getEditText().getText().length() > 0 &&
                mPublisherEditText != null && mPublisherEditText.getEditText() != null && mPublisherEditText.getEditText().getText() != null && mPublisherEditText.getEditText().getText().length() > 0 &&
                mPublishYearSpinner != null && mPublishYearSpinner.getSelectedItemPosition() > 0 &&
                mConditionsSpinner != null && mConditionsSpinner.getSelectedItemPosition() > 0 &&
                mPositionEditText != null && mPositionEditText.getText() != null && mPositionEditText.getText().length() > 0;
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        File cleanStorage = new File(mPhotosPath.get(mUploadedImagesCount));
        cleanStorage.delete();
        mUploadedImagesCount++;
        mDownloadUrls.add(taskSnapshot.getDownloadUrl().toString());

        if (mUploadedImagesCount >= mPhotosPath.size()) {
            // upload book now
            mImagesUploaded = true;
            uploadBookInfo();
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            mStorageRef = storageReference.child(generateStorageRef(mPhotosPath.get(mUploadedImagesCount)));
            byte[] compressedImage = compressPhoto(mPhotosPath.get(mUploadedImagesCount));
            if (compressedImage == null) {
                Toast.makeText(getApplicationContext(),
                        R.string.load_book_upload_error,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            UploadTask uploadTask = mStorageRef.putBytes(compressedImage);
            uploadTask.addOnSuccessListener(this).addOnFailureListener(this);
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        for (int i = mUploadedImagesCount; i > 0; i--) {
            StorageReference child = storageReference.child(generateStorageRef(mPhotosPath.get(i)));
            child.delete();
        }

    }
}
