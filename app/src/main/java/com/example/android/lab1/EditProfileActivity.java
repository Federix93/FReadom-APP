package com.example.android.lab1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class EditProfileActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    Toolbar mToolbar;
    ImageView mSaveProfileUpdatesImageView;
    ImageView mCameraImageView;
    ImageView mUserImageView;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextView mAddressTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;
    ImageView mPositionImageView;

    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;

    private FirebaseAuth mFirebaseAuth;
    private String mCurrentPhotoPath;
    private AlertDialog.Builder mAlertDialogBuilder = null;
    private File mPhotoFile = null;
    private View mFocusedView;
    private Intent showProfileIntent;
    private Map<String, Object> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mUsernameTextInputLayout = findViewById(R.id.username_text_edit);

        mEmailTextInputLayout = findViewById(R.id.email_text_edit);

        mPhoneTextInputLayout = findViewById(R.id.phone_text_edit);

        mUserImageView = findViewById(R.id.user_profile_edit_image);

        mSaveProfileUpdatesImageView = findViewById(R.id.icon_check_toolbar);

        mCameraImageView = findViewById(R.id.add_image_icon);

        mPositionImageView = findViewById(R.id.position_image_view);

        mAddressTextInputLayout = findViewById(R.id.position_text_edit);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        mToolbar = findViewById(R.id.toolbar_edit_activity);
        mToolbar.setTitle(R.string.edit_title_activity);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        showProfileIntent = getIntent();

        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        if(savedInstanceState == null){
            new GUIManager().execute();
        }

        mSaveProfileUpdatesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(view.getContext());
                User user = sharedPreferencesManager.getUser();
                if (user == null)
                    user = User.getInstance();
                if (mUsernameTextInputLayout.getEditText() != null)
                    user.setUsername(mUsernameTextInputLayout.getEditText().getText().toString());
                if (mEmailTextInputLayout.getEditText() != null)
                    user.setEmail(mEmailTextInputLayout.getEditText().getText().toString());
                if (mPhoneTextInputLayout.getEditText() != null)
                    user.setPhone(mPhoneTextInputLayout.getEditText().getText().toString());
                if (mAddressTextInputLayout != null && user.getTempAddress() != null)
                    user.setAddress(mAddressTextInputLayout.getText().toString());
                if (mShortBioTextInputLayout.getEditText() != null)
                    user.setShortBio(mShortBioTextInputLayout.getEditText().getText().toString());
                user.setImage(mCurrentPhotoPath);
                sharedPreferencesManager.putUser(user);
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mCameraImageView.setOnClickListener(new View.OnClickListener() {
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

        mPositionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PositionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        mAddressTextInputLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PositionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        mFocusedView = null;
        mUsernameTextInputLayout.getEditText().setOnFocusChangeListener(this);
        mEmailTextInputLayout.getEditText().setOnFocusChangeListener(this);
        mPhoneTextInputLayout.getEditText().setOnFocusChangeListener(this);
        mShortBioTextInputLayout.getEditText().setOnFocusChangeListener(this);
        mUsernameTextInputLayout.getEditText().clearFocus();
        mEmailTextInputLayout.getEditText().clearFocus();
        mPhoneTextInputLayout.getEditText().clearFocus();
        mShortBioTextInputLayout.getEditText().clearFocus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();
        if (user != null) {
            user.setTempAddress(user.getAddress());
            sharedPreferencesManager.putUser(user);
        }*/
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();
        if (mAddressTextInputLayout != null) {
            if (user != null) {
                mAddressTextInputLayout.setText(user.getTempAddress());
            }
        }*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUsernameTextInputLayout.getEditText() != null)
            outState.putString("Username", mUsernameTextInputLayout.getEditText().getText().toString());
        if (mEmailTextInputLayout.getEditText() != null)
            outState.putString("Email", mEmailTextInputLayout.getEditText().getText().toString());
        if (mPhoneTextInputLayout.getEditText() != null)
            outState.putString("Phone", mPhoneTextInputLayout.getEditText().getText().toString());
        if (mAddressTextInputLayout != null)
            outState.putString("Address", mAddressTextInputLayout.getText().toString());
        if (mShortBioTextInputLayout.getEditText() != null)
            outState.putString("ShortBio", mShortBioTextInputLayout.getEditText().getText().toString());
        if (mPhotoFile != null)
            outState.putString("PhotoFile", mPhotoFile.getAbsolutePath());
        if (mFocusedView != null) {
            if (mFocusedView == mUsernameTextInputLayout.getEditText())
                outState.putString("Focus", "FUsername");
            else if (mFocusedView == mEmailTextInputLayout.getEditText())
                outState.putString("Focus", "FEmail");
            else if (mFocusedView == mPhoneTextInputLayout.getEditText())
                outState.putString("Focus", "FPhone");
            else
                outState.putString("Focus", "FShortBio");
        }
        outState.putString("UriImage", mCurrentPhotoPath);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mUsernameTextInputLayout.getEditText() != null)
            mUsernameTextInputLayout.getEditText().setText(savedInstanceState.getString("Username"));
        if (mEmailTextInputLayout.getEditText() != null)
            mEmailTextInputLayout.getEditText().setText(savedInstanceState.getString("Email"));
        if (mPhoneTextInputLayout.getEditText() != null)
            mPhoneTextInputLayout.getEditText().setText(savedInstanceState.getString("Phone"));
        if (mAddressTextInputLayout != null)
            mAddressTextInputLayout.setText(savedInstanceState.getString("Address"));
        if (mShortBioTextInputLayout.getEditText() != null)
            mShortBioTextInputLayout.getEditText().setText(savedInstanceState.getString("ShortBio"));
        if (savedInstanceState.containsKey("PhotoFile"))
            mPhotoFile = new File(savedInstanceState.getString("PhotoFile"));
        if (savedInstanceState.containsKey("CurrentFocus")) {
            View genericView = findViewById(savedInstanceState.getInt("CurrentFocus"));
            if (genericView != null)
                genericView.requestFocus();
        }
        mCurrentPhotoPath = savedInstanceState.getString("UriImage");
        if (mCurrentPhotoPath != null) {
            Glide.with(getApplicationContext()).load(mCurrentPhotoPath)
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImageView);
        } else {
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImageView);
        }
        if (savedInstanceState.containsKey("Focus")) {
            switch (savedInstanceState.getString("Focus")) {
                case "FUserName":
                    mUsernameTextInputLayout.getEditText().requestFocus();
                    mUsernameTextInputLayout.getEditText()
                            .setSelection(mUsernameTextInputLayout.getEditText().getText().length());
                    break;
                case "FEmail":
                    mEmailTextInputLayout.getEditText().requestFocus();
                    mEmailTextInputLayout.getEditText()
                            .setSelection(mEmailTextInputLayout.getEditText().getText().length());
                    break;
                case "FPhone":
                    mPhoneTextInputLayout.getEditText().requestFocus();
                    mPhoneTextInputLayout.getEditText()
                            .setSelection(mPhoneTextInputLayout.getEditText().getText().length());
                    break;
                case "FShortBio":
                    mShortBioTextInputLayout.getEditText().requestFocus();
                    mShortBioTextInputLayout.getEditText()
                            .setSelection(mShortBioTextInputLayout.getEditText().getText().length());
                    break;
                default:
                    mUsernameTextInputLayout.getEditText().clearFocus();
                    mEmailTextInputLayout.getEditText().clearFocus();
                    mPhoneTextInputLayout.getEditText().clearFocus();
                    mShortBioTextInputLayout.getEditText().clearFocus();
            }
        } else {
            mUsernameTextInputLayout.getEditText().clearFocus();
            mEmailTextInputLayout.getEditText().clearFocus();
            mPhoneTextInputLayout.getEditText().clearFocus();
            mShortBioTextInputLayout.getEditText().clearFocus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imageURI = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                imageURI = data.getData().toString();
                mCurrentPhotoPath = imageURI;
                Glide.with(getApplicationContext()).load(imageURI).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == RESULT_OK && mPhotoFile != null) {
            mCurrentPhotoPath = mPhotoFile.getAbsolutePath();
            Glide.with(getApplicationContext()).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mFocusedView = v;
        } else {
            mFocusedView = null;
        }
    }

    private class GUIManager extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User userNormal = User.getInstance();
                                data = document.getData();
                                if (data != null) {
                                    if (data.get(User.Utils.USERNAME_KEY) != null && mUsernameTextInputLayout.getEditText() != null)
                                        mUsernameTextInputLayout.getEditText().setText(data.get(User.Utils.USERNAME_KEY).toString());
                                    if (data.get(User.Utils.EMAIL_KEY) != null && mEmailTextInputLayout.getEditText() != null)
                                        mEmailTextInputLayout.getEditText().setText(data.get(User.Utils.EMAIL_KEY).toString());
                                    if (data.get(User.Utils.PHONE_KEY) != null && mPhoneTextInputLayout.getEditText() != null)
                                        mPhoneTextInputLayout.getEditText().setText(data.get(User.Utils.PHONE_KEY).toString());
                                    if (data.get(User.Utils.SHORTBIO_KEY) != null && mShortBioTextInputLayout.getEditText() != null)
                                        mShortBioTextInputLayout.getEditText().setText(data.get(User.Utils.SHORTBIO_KEY).toString());
                                    if (mUserImageView != null) {
                                        Glide.with(getApplicationContext()).load(data.get(User.Utils.PICTURE_KEY)).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                                        mCurrentPhotoPath = data.get(User.Utils.PICTURE_KEY).toString();
                                    }

                                    if (mAddressTextInputLayout != null) {
                                        if (showProfileIntent != null && showProfileIntent.hasExtra(MainActivity.ADDRESS_KEY)) {
                                            String address = showProfileIntent.getStringExtra(MainActivity.ADDRESS_KEY);
                                            if (address != null)
                                                mAddressTextInputLayout.setText(address);
                                            else
                                                mAddressTextInputLayout.setText(R.string.selection_position);
                                        } else {
                                            if (userNormal != null && userNormal.getTempAddress() != null) {
                                                mAddressTextInputLayout.setText(userNormal.getTempAddress());
                                            } else {
                                                mAddressTextInputLayout.setText(R.string.selection_position);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            return null;
        }
    }

}
