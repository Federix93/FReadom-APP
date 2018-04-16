package com.example.android.lab1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

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

    private User mUser;

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int RC_SIGN_IN = 10;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
        }

        mSharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                mUser = documentSnapshot.toObject(User.class);
                if (mUser != null) {
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
                    } else {
                        mSharedPreferencesManager.putImage(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp).toString());
                        mUser.setImage(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp).toString());
                    }

                    Glide.with(getApplicationContext()).load(mUser.getImage())
                            .apply(bitmapTransform(new CircleCrop()))
                            .into(mCircleImageView);

                    if (mAddressTextInputLayout.getEditText() != null) {
                        if (mUser.getAddress() != null)
                            mAddressTextInputLayout.getEditText().setText(mUser.getAddress());
                        mAddressTextInputLayout.getEditText().setKeyListener(null);
                        mAddressTextInputLayout.getEditText().setEnabled(false);
                    }
                }
            }
        });

        //TODO: Ricordarsi che l'immagine del login potrebbe essere diversa da quella del cloud
        /*if(mFirebaseAuth.getCurrentUser() != null) {
            DocumentReference docRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("LULLO", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        data = snapshot.getData();
                        if(data == null)
                            return;
                        if (mUsernameTextInputLayout.getEditText() != null) {
                            if (data.get(User.Utils.USERNAME_KEY) != null)
                                mUsernameTextInputLayout.getEditText().setText(data.get(User.Utils.USERNAME_KEY).toString());
                            mUsernameTextInputLayout.getEditText().setKeyListener(null);
                            mUsernameTextInputLayout.getEditText().setEnabled(false);
                        }
                        if (mEmailTextInputLayout.getEditText() != null) {
                            if (data.get(User.Utils.EMAIL_KEY) != null)
                                mEmailTextInputLayout.getEditText().setText(data.get(User.Utils.EMAIL_KEY).toString());
                            mEmailTextInputLayout.getEditText().setKeyListener(null);
                            mEmailTextInputLayout.getEditText().setEnabled(false);
                        }
                        if (mPhoneTextInputLayout.getEditText() != null) {
                            if (data.get(User.Utils.PHONE_KEY) != null)
                                mPhoneTextInputLayout.getEditText().setText(data.get(User.Utils.PHONE_KEY).toString());
                            mPhoneTextInputLayout.getEditText().setKeyListener(null);
                            mPhoneTextInputLayout.getEditText().setEnabled(false);
                        }

                        if (mShortBioTextInputLayout.getEditText() != null) {
                            if (data.get(User.Utils.SHORTBIO_KEY) != null)
                                mShortBioTextInputLayout.getEditText().setText(data.get(User.Utils.SHORTBIO_KEY).toString());
                            mShortBioTextInputLayout.getEditText().setKeyListener(null);
                            mShortBioTextInputLayout.getEditText().setEnabled(false);
                        }
                        if(data.get(User.Utils.PICTURE_KEY) != null) {
                            Glide.with(getApplicationContext()).load(data.get(User.Utils.PICTURE_KEY))
                                    .apply(bitmapTransform(new CircleCrop()))
                                    .into(mCircleImageView);
                        }else {
                            Glide.with(getApplicationContext()).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                                    .apply(bitmapTransform(new CircleCrop()))
                                    .into(mCircleImageView);
                        }
                        if (mAddressTextInputLayout.getEditText() != null) {
                            if (data.get(User.Utils.POSITION_KEY) != null)
                                mAddressTextInputLayout.getEditText().setText(data.get(User.Utils.POSITION_KEY).toString());
                            mAddressTextInputLayout.getEditText().setKeyListener(null);
                            mAddressTextInputLayout.getEditText().setEnabled(false);
                        }
                    } else {

                    }
                }
            });
        }*/

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }
}
