package com.example.android.lab1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class MainActivity extends AppCompatActivity {

    ImageView mToolbarIcon;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextInputLayout mAddressTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;
    ImageView mCircleImageView;
    Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDb;
    private User mUser;
    private DocumentReference mUserDocumentReference;

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 10;
    SharedPreferencesManager mSharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        mUsernameTextInputLayout = findViewById(R.id.username_text);

        mPhoneTextInputLayout = findViewById(R.id.phone_text);

        mEmailTextInputLayout = findViewById(R.id.email_text);

        mAddressTextInputLayout = findViewById(R.id.address_text_view);

        mCircleImageView = findViewById(R.id.profile_image);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        mToolbarIcon = findViewById(R.id.icon_toolbar);

        mToolbar = findViewById(R.id.toolbar_main_activity);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);
        mUserDocumentReference = mDb.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
        mSharedPreferencesManager = SharedPreferencesManager.getInstance(this);

        mToolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (mFirebaseAuth.getCurrentUser() != null)
                    if (mUser != null)
                        intent.putExtra(ADDRESS_KEY, mUser.getAddress());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserDocumentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                mUser = documentSnapshot.toObject(User.class);
                checkPermissionToLoadPicture();
                if (mUser != null) {
                    updateUI();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updatePicture();
                }
                else{
                    updateWithDefaultPicture();
                }
                break;
        }
    }

    private void updateUI(){
        if (mUsernameTextInputLayout.getEditText() != null) {
            if (mUser.getUsername() != null)
                mUsernameTextInputLayout.getEditText().setText(mUser.getUsername());
            mUsernameTextInputLayout.getEditText().setKeyListener(null);
            mUsernameTextInputLayout.getEditText().setEnabled(false);
        }
        if (mEmailTextInputLayout.getEditText() != null) {
            if (mUser.getEmail() != null)
                mEmailTextInputLayout.getEditText().setText(mUser.getEmail());
            mEmailTextInputLayout.getEditText().setKeyListener(null);
            mEmailTextInputLayout.getEditText().setEnabled(false);
        }
        if (mPhoneTextInputLayout.getEditText() != null) {
            if (mUser.getPhone() != null)
                mPhoneTextInputLayout.getEditText().setText(mUser.getPhone());
            mPhoneTextInputLayout.getEditText().setKeyListener(null);
            mPhoneTextInputLayout.getEditText().setEnabled(false);
        }

        if (mShortBioTextInputLayout.getEditText() != null) {
            if (mUser.getShortBio() != null)
                mShortBioTextInputLayout.getEditText().setText(mUser.getShortBio());
            mShortBioTextInputLayout.getEditText().setKeyListener(null);
            mShortBioTextInputLayout.getEditText().setEnabled(false);
        }
        if (mUser.getImage() != null) {
            mSharedPreferencesManager.putImage(mUser.getImage());

        }
        if (mAddressTextInputLayout.getEditText() != null) {
            if (mUser.getAddress() != null)
                mAddressTextInputLayout.getEditText().setText(mUser.getAddress());
            mAddressTextInputLayout.getEditText().setKeyListener(null);
            mAddressTextInputLayout.getEditText().setEnabled(false);
        }
    }
    private void updatePicture(){
        if(mUser.getImage() != null) {
            Glide.with(getApplicationContext()).load(mUser.getImage())
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        }else{
            updateWithDefaultPicture();
        }
    }

    private void updateWithDefaultPicture(){
        Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                .apply(bitmapTransform(new CircleCrop()))
                .into(mCircleImageView);
    }

    private void checkPermissionToLoadPicture(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION);
        }else{
            updatePicture();
        }
    }
}
