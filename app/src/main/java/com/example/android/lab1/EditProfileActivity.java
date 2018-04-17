package com.example.android.lab1;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class EditProfileActivity extends AppCompatActivity implements View.OnFocusChangeListener, EventListener<DocumentSnapshot> {

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

    private User mUser;

    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;

    private FirebaseAuth mFirebaseAuth;
    private String mCurrentPhotoPath;
    private AlertDialog.Builder mAlertDialogBuilder = null;
    private File mPhotoFile = null;
    private View mFocusedView;
    private Intent showProfileIntent;

    DocumentReference mUserRef;
    ListenerRegistration mUserListenerRegistration;
    SharedPreferencesManager mSharedPreferencesManager;

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

        /*Firebase*/
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        mSharedPreferencesManager = SharedPreferencesManager.getInstance(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mUserRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());

        Log.d("LULLO", "SharedPref: " + mSharedPreferencesManager.getImage());
        mCurrentPhotoPath = mSharedPreferencesManager.getImage();

        mSaveProfileUpdatesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build();
                db.setFirestoreSettings(settings);
                final DocumentReference docRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
                db.collection("users").whereEqualTo(mFirebaseAuth.getCurrentUser().getUid(), docRef)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    return;
                                }
                                if (mUsernameTextInputLayout.getEditText() != null)
                                    mUser.setUsername(mUsernameTextInputLayout.getEditText().getText().toString());
                                if (mEmailTextInputLayout.getEditText() != null)
                                    mUser.setEmail(mEmailTextInputLayout.getEditText().getText().toString());
                                if (mPhoneTextInputLayout.getEditText() != null)
                                    mUser.setPhone(mPhoneTextInputLayout.getEditText().getText().toString());
                                if (mAddressTextInputLayout != null && mUser.getTempAddress() != null)
                                    mUser.setAddress(mAddressTextInputLayout.getText().toString());
                                if (mShortBioTextInputLayout.getEditText() != null)
                                    mUser.setShortBio(mShortBioTextInputLayout.getEditText().getText().toString());
                                mUser.setImage(mCurrentPhotoPath);
                                docRef.set(mUser, SetOptions.merge());
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        });
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
        resetFocus();
        clearFocusOnViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
            Log.d("LULLO", "3 " + mCurrentPhotoPath);
            Glide.with(this).load(mCurrentPhotoPath)
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
                    clearFocusOnViews();
            }
        } else {
            clearFocusOnViews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                mCurrentPhotoPath = data.getData().toString();
                mSharedPreferencesManager.putImage(mCurrentPhotoPath);
                Glide.with(getApplicationContext()).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == RESULT_OK && mPhotoFile != null) {
            mCurrentPhotoPath = mPhotoFile.getAbsolutePath();
            mSharedPreferencesManager.putImage(mCurrentPhotoPath);
            Glide.with(this).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserListenerRegistration = mUserRef.addSnapshotListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUserListenerRegistration != null) {
            mUserListenerRegistration.remove();
            mUserListenerRegistration = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mFocusedView = v;
        } else {
            mFocusedView = null;
        }
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            return;
        }
        mUser = documentSnapshot.toObject(User.class);
        onUserLoaded(mUser);
    }

    private void onUserLoaded(User user) {
        if (user != null) {
            if (mUsernameTextInputLayout.getEditText() != null)
                mUsernameTextInputLayout.getEditText().setText(user.getUsername());
            if (mEmailTextInputLayout.getEditText() != null)
                mEmailTextInputLayout.getEditText().setText(user.getEmail());
            if (mPhoneTextInputLayout.getEditText() != null)
                mPhoneTextInputLayout.getEditText().setText(user.getPhone());
            if (mShortBioTextInputLayout.getEditText() != null)
                mShortBioTextInputLayout.getEditText().setText(user.getShortBio());
            if (mUserImageView != null) {
                if(mCurrentPhotoPath != null) {
                    Log.d("LULLO", "1 " + mCurrentPhotoPath);
                    Glide.with(this).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                }else {
                    Log.d("LULLO", "2");
                    Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                            .apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                }
            }

            if (mAddressTextInputLayout != null) {
                if (showProfileIntent != null && showProfileIntent.hasExtra(MainActivity.ADDRESS_KEY)) {
                    String address = showProfileIntent.getStringExtra(MainActivity.ADDRESS_KEY);
                    if (address != null)
                        mAddressTextInputLayout.setText(address);
                    else
                        mAddressTextInputLayout.setText(R.string.selection_position);
                } else {
                    if (user.getTempAddress() != null) {
                        mAddressTextInputLayout.setText(user.getTempAddress());
                    } else {
                        mAddressTextInputLayout.setText(R.string.selection_position);
                    }
                }
            }
        }
    }

    private void clearFocusOnViews() {
        if (mUsernameTextInputLayout.getEditText() != null)
            mUsernameTextInputLayout.getEditText().clearFocus();
        if (mEmailTextInputLayout.getEditText() != null)
            mEmailTextInputLayout.getEditText().clearFocus();
        if (mPhoneTextInputLayout.getEditText() != null)
            mPhoneTextInputLayout.getEditText().clearFocus();
        if (mShortBioTextInputLayout.getEditText() != null)
            mShortBioTextInputLayout.getEditText().clearFocus();
    }

    private void resetFocus() {
        if (mUsernameTextInputLayout.getEditText() != null)
            mUsernameTextInputLayout.getEditText().setOnFocusChangeListener(this);
        if (mEmailTextInputLayout.getEditText() != null)
            mEmailTextInputLayout.getEditText().setOnFocusChangeListener(this);
        if (mPhoneTextInputLayout.getEditText() != null)
            mPhoneTextInputLayout.getEditText().setOnFocusChangeListener(this);
        if (mShortBioTextInputLayout.getEditText() != null)
            mShortBioTextInputLayout.getEditText().setOnFocusChangeListener(this);
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
        return image;
    }

}
