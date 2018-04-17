package com.example.android.lab1;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.lab1.model.Condition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadBookActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ISBN_REQUEST_TAG = "ISBN_REQUEST";
    private static final int SCAN_REQUEST_TAG = 3;
    private static final int POSITION_REQUEST = 2;

    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;

    private static final String ISBN_FIELD = "ISBN";
    private static final String TITLE_FIELD = "TITLE";
    private static final String AUTHOR_FIELD = "AUTHOR";
    private static final String PUBLISHER_FIELD = "PUBLISHER";
    private static final String POSITION_FIELD = "POSITION";
    private static final String WEB_THUMBNAIL_FIELD = "WEB_THUMBNAIL";
    private static final String PHOTOS_PATH_FIELD = "PHOTOS_PATH";

    private ViewPager mGallery;
    private ImageView mAddImageIcon;
    private EditText mIsbnEditText;
    private ImageView mIsbnScanBarCode;
    private ImageView mIsbnImageView;
    private EditText mTitleEditText;
    private EditText mAuthorEditText;
    private EditText mPublisherEditText;
    private TextView mPublishYearTextView;
    private Spinner mPublishYearSpinner;
    private TextView mConditionsTextView;
    private Spinner mConditionsSpinner;
    private TextView mPositionEditText;
    private ImageView mPositionIcon;
    private Toolbar mToolbar;
    private ImageView mConfirmImageView;
    private AlertDialog.Builder mAlertDialogBuilder;
    private File mPhotoFile; // last photo thumbnail
    private LinearLayout mDotsContainer;

    private ArrayList<String> mPhotosPath;
    private String mWebThumbnail;
    private RequestQueue mRequestQueue;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_book);

        mGallery = findViewById(R.id.load_book_gallery);
        mAddImageIcon = findViewById(R.id.add_image_icon);
        mIsbnEditText = findViewById(R.id.load_book_isbn);
        mIsbnScanBarCode = findViewById(R.id.load_book_scan_bar_code);
        mIsbnImageView = findViewById(R.id.load_book_auto_fill);
        mTitleEditText = findViewById(R.id.load_book_title);
        mAuthorEditText = findViewById(R.id.load_book_author);
        mPublisherEditText = findViewById(R.id.load_book_publisher);
        mPublishYearTextView = findViewById(R.id.load_book_publish_year_text_view);
        mPublishYearSpinner = findViewById(R.id.load_book_publish_year_spinner);
        mConditionsTextView = findViewById(R.id.load_book_conditions);
        mConditionsSpinner = findViewById(R.id.load_book_conditions_spinner);
        mPositionEditText = findViewById(R.id.load_book_position);
        mPositionIcon = findViewById(R.id.load_book_location);
        mToolbar = findViewById(R.id.toolbar_loadbook);
        mConfirmImageView = findViewById(R.id.icon_check_toolbar);
        mDotsContainer = findViewById(R.id.dots_container);

        // toolbar
        mToolbar.setTitle(R.string.load_book_toolbar_title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete photos
                for (String s : mPhotosPath) {
                    File f = new File(s);
                    if (f.isFile())
                        deleteFile(f.getAbsolutePath());
                }
                finish();
            }
        });
        setSupportActionBar(mToolbar);

        // spinners

        // year spinner
        String years[] = new String[200];

        for (int i = Calendar.getInstance().get(Calendar.YEAR), n = 0; n < 200; i--, n++)
            years[n] = Integer.toString(i);

        mPublishYearSpinner.setAdapter(makeDropDownAdapter(years));

        Condition.Status[] status = Condition.Status.values();
        String conditions[] = new String[status.length];
        int i = 0;
        for (Condition.Status s : status) {
            conditions[i++] = getString(Condition.getResource(s));
        }

        mConditionsSpinner.setAdapter(makeDropDownAdapter(conditions));

        mAddImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                    startActivityForResult(cameraIntent, CAPTURE_IMAGE);
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

        // ISBN auto fill listener
        setIsbnListeners();

        // position listener
        mPositionEditText.setOnClickListener(this);
        mPositionIcon.setOnClickListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(PHOTOS_PATH_FIELD))
            mPhotosPath = savedInstanceState.getStringArrayList(PHOTOS_PATH_FIELD);
        else
            mPhotosPath = new ArrayList<>();

        mConfirmImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // confirm data and send back a book
            }
        });

        mGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TextView dot;
                for (int i = 0; i < mDotsContainer.getChildCount(); i++)
                {
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
    }


    private void makeRequestBookApi(final String isbn, final boolean alternativeEndPoint) {
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

                        mTitleEditText.setText(book.optString("title"));
                        if (book.has("authors") &&
                                book.getJSONArray("authors").length() > 0)
                            mAuthorEditText.setText(book.getJSONArray("authors")
                                    .getString(0));
                        else
                            mAuthorEditText.setText("");
                        mPublisherEditText.setText(book.optString("publisher"));

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
                                ;
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
            outState.putString(ISBN_FIELD, mIsbnEditText.getText().toString());
        if (mTitleEditText != null)
            outState.putString(TITLE_FIELD, mTitleEditText.getText().toString());
        if (mAuthorEditText != null)
            outState.putString(AUTHOR_FIELD, mAuthorEditText.getText().toString());
        if (mPublisherEditText != null)
            outState.putString(PUBLISHER_FIELD, mPublisherEditText.getText().toString());
        if (mPositionEditText != null && mPositionEditText.getText() != null)
            outState.putString(POSITION_FIELD, mPositionEditText.getText().toString());
        if (mWebThumbnail != null)
            outState.putString(WEB_THUMBNAIL_FIELD, mWebThumbnail);
        if (mPhotosPath != null)
            outState.putStringArrayList(PHOTOS_PATH_FIELD, mPhotosPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(ISBN_FIELD))
            mIsbnEditText.setText(savedInstanceState.getString(ISBN_FIELD));
        if (savedInstanceState.containsKey(TITLE_FIELD))
            mTitleEditText.setText(savedInstanceState.getString(TITLE_FIELD));
        if (savedInstanceState.containsKey(AUTHOR_FIELD))
            mAuthorEditText.setText(savedInstanceState.getString(AUTHOR_FIELD));
        if (savedInstanceState.containsKey(PUBLISHER_FIELD))
            mPublisherEditText.setText(savedInstanceState.getString(PUBLISHER_FIELD));
        if (savedInstanceState.containsKey(POSITION_FIELD))
            mPositionEditText.setText(savedInstanceState.getString(POSITION_FIELD));
        if (savedInstanceState.containsKey(WEB_THUMBNAIL_FIELD))
            mWebThumbnail = savedInstanceState.getString(WEB_THUMBNAIL_FIELD);
        if (savedInstanceState.containsKey(PHOTOS_PATH_FIELD)) {
            mPhotosPath = savedInstanceState.getStringArrayList(PHOTOS_PATH_FIELD);
            mGallery.setAdapter(new ImagePagerAdapter(getApplicationContext(), mPhotosPath, (LinearLayout) findViewById(R.id.dots_container)));
        }
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

    private void setIsbnListeners() {
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

        mIsbnEditText.addTextChangedListener(new TextWatcher() {
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
                        validateIsbn(mIsbnEditText);
                } else {
                    // deleting
                    validateIsbn(mIsbnEditText);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mIsbnEditText.setFilters(new InputFilter[]{
                new InputFilter() {
                    Pattern mPattern = Pattern.compile("^[-0123456789]");

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        Matcher m = mPattern.matcher(source);
                        return m.matches() ? null : "";
                    }
                }
        });

        // Autofill icon
        mIsbnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateIsbn(mIsbnEditText)) {
                    makeRequestBookApi(mIsbnEditText.getText().toString(), false);
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
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SCAN_REQUEST_TAG:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBarCodeScanner();
                    break;
                }
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
                        if (!mIsbnEditText.getText().toString().equals(resultData.getStringExtra(ScanBarCodeActivity.BARCODE_KEY))) {
                            // disable input filters temporarely
                            InputFilter[] filters = mIsbnEditText.getFilters();
                            mIsbnEditText.setFilters(new InputFilter[]{});
                            mIsbnEditText.setText(resultData.getStringExtra(ScanBarCodeActivity.BARCODE_KEY));
                            mIsbnEditText.setFilters(filters);
                            validateIsbn(mIsbnEditText);
                            mTitleEditText.setText("");
                            mAuthorEditText.setText("");
                            mPublisherEditText.setText("");
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
                    if (mPhotoFile != null && mPhotoFile.isFile())
                        deleteFile(mPhotoFile.getAbsolutePath());
                }
                // TODO Add action result for photo picker
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
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void updateGallery(String newPhoto, Integer optPosition) {
        boolean customPosition;
        if (optPosition == null) {
            mPhotosPath.add(newPhoto);
        } else if (optPosition >= 0 && optPosition < mPhotosPath.size())
            mPhotosPath.add(optPosition, newPhoto);
        else
            optPosition = null;
        mGallery.setAdapter(new ImagePagerAdapter(getApplicationContext(), mPhotosPath, (LinearLayout) findViewById(R.id.dots_container)));
        mGallery.setCurrentItem(optPosition == null ? mPhotosPath.size() - 1 : optPosition, true);
    }

}
