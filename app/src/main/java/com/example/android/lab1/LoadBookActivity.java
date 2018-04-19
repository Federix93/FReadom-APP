package com.example.android.lab1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
    private static final String WEB_THUMBNAIL_FIELD = "WEB_THUMBNAIL";
    private static final String PHOTOS_PATH_FIELD = "PHOTOS_PATH";
    private static final String CURRENT_PHOTO = "CURRENT_PHOTO";
    private static final String CURRENT_STORAGE_REF = "CURRENT_STORAGE_REF";
    private static final String IMAGE_DOWNLOAD_URLS = "IMAGE_DOWNLOAD_URLS";
    private static final String UPLOADED_IMAGE_COUNT = "UPLOADED_IMAGES_COUNT";
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
        mPositionIcon = findViewById(R.id.load_book_location);
        mToolbar = findViewById(R.id.toolbar_loadbook);
        mConfirmImageView = findViewById(R.id.icon_check_toolbar);
        mDotsContainer = findViewById(R.id.dots_container);

        // TODO add recieve intent to modify book


        // complex listeners are handled in separate function
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

                        lockUI();
                        mIsbnImageView.setVisibility(View.GONE);
                        final ProgressBar mProgressBar = findViewById(R.id.load_book_progress_bar);
                        mProgressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),
                                R.string.load_book_upload,
                                Toast.LENGTH_SHORT)
                                .show();

                        if (mPhotosPath != null && mPhotosPath.size() > 0)
                            uploadPhotos(mPhotosPath);
                        else
                            uploadBookInfo();


                    }
                }
                return true;

            }
        });
    }

    private void uploadPhotos(final ArrayList<String> mPhotosPath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        mUploadedImagesCount = 0;
        if (mPhotosPath.size() > 0) {
            mDownloadUrls = new ArrayList<>();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String lastPathSegment = Uri.fromFile(new File(mPhotosPath.get(mUploadedImagesCount))).getLastPathSegment();
            String lastPathHash = getSha1Hex(lastPathSegment + Long.toString(System.currentTimeMillis()));

            mStorageRef = storageReference.child(uid + "/images/books/" + lastPathHash + "-" + lastPathSegment + ".jpg");

            // compress current image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(mPhotosPath.get(0), options);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            byte[] compressedImage = out.toByteArray();

            UploadTask uploadTask = mStorageRef.putBytes(compressedImage);
            uploadTask.addOnSuccessListener(this).addOnFailureListener(this);
        }
    }

    private void uploadBookInfo() {

        Condition condition;
        String conditionText = mConditionsSpinner.getSelectedItem().toString();
        if (conditionText.equals(getString(R.string.bad)))
            condition = new Condition(Condition.Status.BAD);
        else if (conditionText.equals(getString(R.string.decent)))
            condition = new Condition(Condition.Status.DECENT);
        else if (conditionText.equals(getString(R.string.good)))
            condition = new Condition(Condition.Status.GOOD);
        else if (conditionText.equals(getString(R.string.great)))
            condition = new Condition(Condition.Status.GREAT);
        else
            condition = new Condition(Condition.Status.NEW);

        final Book bookToLoad = new Book();
        bookToLoad.setIsbn(mIsbnEditText.getEditText().getText().toString());
        bookToLoad.setTitle(mTitleEditText.getEditText().getText().toString());
        bookToLoad.setAuthor(mAuthorEditText.getEditText().getText().toString());
        bookToLoad.setPublisher(mPublisherEditText.getEditText().getText().toString());
        bookToLoad.setPublishYear(Integer.parseInt(mPublishYearSpinner.getSelectedItem().toString()));
        bookToLoad.setConditions(condition);
        bookToLoad.setAddress(mPositionEditText.getText().toString());
        //bookToLoad.setConditions(condition);
        bookToLoad.setUid(mFirebaseAuth.getCurrentUser().getUid());

        if (mWebThumbnail != null)
            bookToLoad.setThumbnail(mWebThumbnail);
        if (mDownloadUrls != null)
            bookToLoad.setBookImagesUrls(mDownloadUrls);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books").add(bookToLoad).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // using documentReference create a folder on storage for storing photos
                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
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

    private void lockUI() {
        // this methods disable all UI controls while downloads or uploads are happening
        // TODO
    }

    private void unLockUI() {
        // TODO revert above method
    }

    private void setupSpinners() {
        String years[] = new String[200];

        for (int i = Calendar.getInstance().get(Calendar.YEAR), n = 0; n < 200; i--, n++)
            years[n] = Integer.toString(i);

        mPublishYearSpinner.setAdapter(makeDropDownAdapter(years));
        mPublishYearSpinner.setHint(R.string.publishing_year);
        mPublishYearSpinner.setFloatingLabelText(R.string.publishing_year);

        Condition.Status[] status = Condition.Status.values();
        String conditions[] = new String[status.length];
        int i = 0;
        for (Condition.Status s : status) {
            conditions[i++] = getString(Condition.getResource(s));
        }

        mConditionsSpinner.setAdapter(makeDropDownAdapter(conditions));
        mConditionsSpinner.setHint(R.string.conditions);
        mConditionsSpinner.setFloatingLabelText(R.string.conditions);
    }

    private void setupGalleryListeners() {
        mGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Log.d("DEBUBDOTS", "onPageSelected: selectedPage: " + Integer.toString(position));
                TextView dot;
                //Log.d("DEBUBDOTS", "onPageSelected: numOfDots: " + Integer.toString(mDotsContainer.getChildCount()));
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
        super.onRestoreInstanceState(savedInstanceState);
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
        Log.d("LULLO", "ISSSSSSS: " + (mIsbnEditText.getEditText().getText() != null && mIsbnEditText.getEditText().getText().length() > 0 && mIsbnEditText.getError() == null &&
                mTitleEditText.getEditText().getText() != null && mTitleEditText.getEditText().getText().length() > 0 &&
                mAuthorEditText.getEditText().getText() != null && mAuthorEditText.getEditText().getText().length() > 0 &&
                mPublisherEditText.getEditText().getText() != null && mPublisherEditText.getEditText().getText().length() > 0 &&
                mPositionEditText.getText() != null && mPositionEditText.getText().length() > 0));
        return mIsbnEditText != null && mIsbnEditText.getEditText() != null && mIsbnEditText.getEditText().getText() != null && mIsbnEditText.getEditText().getText().length() > 0 && mIsbnEditText.getError() == null &&
                mTitleEditText != null && mTitleEditText.getEditText() != null && mTitleEditText.getEditText().getText() != null && mTitleEditText.getEditText().getText().length() > 0 &&
                mAuthorEditText != null && mAuthorEditText.getEditText() != null && mAuthorEditText.getEditText().getText() != null && mAuthorEditText.getEditText().getText().length() > 0 &&
                mPublisherEditText != null && mPublisherEditText.getEditText() != null && mPublisherEditText.getEditText().getText() != null && mPublisherEditText.getEditText().getText().length() > 0 &&
                mPublishYearSpinner != null && !((String) mPublishYearSpinner.getItemAtPosition(mPublishYearSpinner.getSelectedItemPosition())).isEmpty() &&
                mConditionsSpinner != null && !((String) mConditionsSpinner.getItemAtPosition(mConditionsSpinner.getSelectedItemPosition())).isEmpty() &&
                mPositionEditText != null && mPositionEditText.getText() != null && mPositionEditText.getText().length() > 0;
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        mUploadedImagesCount++;
        if (mUploadedImagesCount >= mPhotosPath.size()) {
            // upload book now
            uploadBookInfo();
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String lastPathSegment = Uri.fromFile(new File(mPhotosPath.get(mUploadedImagesCount))).getLastPathSegment();
            String lastPathHash = getSha1Hex(lastPathSegment + Long.toString(System.currentTimeMillis()));

            mStorageRef = storageReference.child(uid + "/images/books/" + lastPathHash + "-" + lastPathSegment + ".jpg");

            // compress current image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(mPhotosPath.get(0), options);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            byte[] compressedImage = out.toByteArray();

            UploadTask uploadTask = mStorageRef.putBytes(compressedImage);
            uploadTask.addOnSuccessListener(this).addOnFailureListener(this);
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        // TODO delete uploaded images

    }
}
