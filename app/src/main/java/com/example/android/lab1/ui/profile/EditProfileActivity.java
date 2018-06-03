package com.example.android.lab1.ui.profile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.SignInActivity;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.example.android.lab1.utils.Utilities;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class EditProfileActivity extends AppCompatActivity implements View.OnFocusChangeListener, EventListener<DocumentSnapshot> {

    private static final String CHANGE_PICTURE = "CP";
    Toolbar mToolbar;
    ImageView mCameraImageView;
    ImageView mUserImageView;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;

    private User mUser;

    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;

    private FirebaseAuth mFirebaseAuth;
    private String mCurrentPhotoPath;
    private AlertDialog.Builder mAlertDialogBuilder = null;
    private File mPhotoFile = null;
    private View mFocusedView;

    DocumentReference mUserRef;
    ListenerRegistration mUserListenerRegistration;
    SharedPreferencesManager mSharedPreferencesManager;
    Bundle mSavedInstanceState;

    private boolean mChangedPicture;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mSavedInstanceState = savedInstanceState;

        mUsernameTextInputLayout = findViewById(R.id.username_text_edit);

        mEmailTextInputLayout = findViewById(R.id.email_text_edit);

        mPhoneTextInputLayout = findViewById(R.id.phone_text_edit);

        mUserImageView = findViewById(R.id.user_profile_edit_image);

        mCameraImageView = findViewById(R.id.add_image_fab);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        Utilities.setupStatusBarColor(this);

        mToolbar = findViewById(R.id.toolbar_edit_activity);
        mToolbar.setTitle(R.string.edit_title_activity);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.inflateMenu(R.menu.activity_edit_profile_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clicked = item.getItemId();
                if(clicked == R.id.confirm_updates) {
                    if (mChangedPicture && mCurrentPhotoPath != null) {
                        final ProgressDialogHolder progressDialogHolder = new ProgressDialogHolder(EditProfileActivity.this);
                        progressDialogHolder.showLoadingDialog(R.string.saving);
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference reference = storage.getReference(FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getUid() + "/" + "profile_pic.jpg");
                        byte[] bytes = Utilities.compressPhoto(mCurrentPhotoPath,
                                getContentResolver(),
                                getApplicationContext());
                        if (bytes != null)
                            reference.putBytes(bytes)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    if (mUser != null) {
                                                        mUser.setImage(uri.toString());
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
                                                                        if (mShortBioTextInputLayout.getEditText() != null)
                                                                            mUser.setShortBio(mShortBioTextInputLayout.getEditText().getText().toString());
                                                                        docRef.set(mUser, SetOptions.merge());

                                                                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                                                        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(mFirebaseAuth.getUid());
                                                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                com.example.android.lab1.model.chatmodels.User user = dataSnapshot.getValue(com.example.android.lab1.model.chatmodels.User.class);
                                                                                user.setPhotoURL(mUser.getImage());
                                                                                user.setUsername(mUser.getUsername());
                                                                                databaseReference.setValue(user);
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                        if(progressDialogHolder.isProgressDialogShowing())
                                                                            progressDialogHolder.dismissDialog();
                                                                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                                                        intent.putExtra("user", mUser);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                        startActivity(intent);
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                        }
                                    });
                    } else {
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
                                        if (mShortBioTextInputLayout.getEditText() != null)
                                            mUser.setShortBio(mShortBioTextInputLayout.getEditText().getText().toString());
                                        docRef.set(mUser, SetOptions.merge());
                                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                        intent.putExtra("user", mUser);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                    }
                }
                return true;
            }

        });
        /*Firebase*/
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth == null || mFirebaseAuth.getCurrentUser() == null) {

            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        mSharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mUserRef = db.collection("users").document(mFirebaseAuth.getUid());

        mCurrentPhotoPath = mSharedPreferencesManager.getImage();

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
                                if (!Utilities.checkPermissionActivity(EditProfileActivity.this,
                                        Manifest.permission.CAMERA)) {
                                    Utilities.askPermissionActivity(EditProfileActivity.this,
                                            Manifest.permission.CAMERA, CAPTURE_IMAGE);
                                } else {
                                    takePicture();
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

        mFocusedView = null;
        resetFocus();
        mChangedPicture = false;
        clearFocusOnViews();
    }

//    private void openPositionActivity(boolean skipCheck) {
//        if (skipCheck ||
//                Utilities.checkPermissionActivity(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            Intent positionActivityIntent = Utilities.getPositionActivityIntent(this, false);
//            startActivityForResult(positionActivityIntent, Constants.POSITION_ACTIVITY_REQUEST);
//        } else {
//            Utilities.askPermissionActivity(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Constants.POSITION_ACTIVITY_REQUEST);
//        }
//
//    }

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
        outState.putBoolean(CHANGE_PICTURE, mChangedPicture);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }
        }
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
            Glide.with(this).load(mCurrentPhotoPath)
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImageView);
        } else {
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImageView);
        }
        mChangedPicture = savedInstanceState.getBoolean(CHANGE_PICTURE, false);
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
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    if (data.getData() != null) {
                        mCurrentPhotoPath = data.getData().toString();
                        mSharedPreferencesManager.putImage(mCurrentPhotoPath);
                        Glide.with(getApplicationContext()).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                        mChangedPicture = true;
                    }
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mCurrentPhotoPath = mPhotoFile.getAbsolutePath();
                    mSharedPreferencesManager.putImage(mCurrentPhotoPath);
                    Glide.with(this).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                    mChangedPicture = true;
                }
                break;
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
            if(mSavedInstanceState == null) {
                if (mUsernameTextInputLayout.getEditText() != null)
                    mUsernameTextInputLayout.getEditText().setText(user.getUsername());
                if (mEmailTextInputLayout.getEditText() != null)
                    mEmailTextInputLayout.getEditText().setText(user.getEmail());
                if (mPhoneTextInputLayout.getEditText() != null)
                    mPhoneTextInputLayout.getEditText().setText(user.getPhone());
                if (mShortBioTextInputLayout.getEditText() != null)
                    mShortBioTextInputLayout.getEditText().setText(user.getShortBio());
                if (mUserImageView != null) {
                    if (mCurrentPhotoPath != null) {
                        Glide.with(this).load(mCurrentPhotoPath).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                    } else {
                        Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                                .apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
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
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void takePicture() {
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
}
