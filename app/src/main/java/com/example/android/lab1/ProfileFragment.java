package com.example.android.lab1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ProfileFragment extends Fragment {

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 10;

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
    SharedPreferencesManager mSharedPreferencesManager;

    public ProfileFragment() {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mUsernameTextInputLayout = rootView.findViewById(R.id.username_text);

        mPhoneTextInputLayout = rootView.findViewById(R.id.phone_text);

        mEmailTextInputLayout = rootView.findViewById(R.id.email_text);

        mAddressTextInputLayout = rootView.findViewById(R.id.address_text_view);

        mCircleImageView = rootView.findViewById(R.id.profile_image);

        mShortBioTextInputLayout = rootView.findViewById(R.id.bio_text_edit);

        if(getActivity() != null)
            mToolbar = getActivity().findViewById(R.id.toolbar_main_activity);
        mToolbar.setTitle(R.string.title_profile);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_profile);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                if (clickedId == R.id.action_edit) {
                    Intent intent = new Intent(rootView.getContext(), EditProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    if (mFirebaseAuth.getCurrentUser() != null) {
                        if (mUser != null) {
                            intent.putExtra(ADDRESS_KEY, mUser.getAddress());
                        }
                    }
                    startActivity(intent);
                }
                return true;
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);
        if(mFirebaseAuth.getCurrentUser() != null)
            mUserDocumentReference = mDb.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
        mSharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());

        // Return the rootView
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getActivity() != null) {
            if(mUserDocumentReference != null) {
                mUserDocumentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updatePicture();
                } else {
                    updateWithDefaultPicture();
                }
                break;
        }
    }

    private void updateUI() {
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

    private void updatePicture() {
        if (mUser.getImage() != null) {
            if(getActivity() != null) {
                Glide.with(getActivity()).load(mUser.getImage())
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mCircleImageView);
            }
        } else {
            updateWithDefaultPicture();
        }
    }

    private void updateWithDefaultPicture() {
        Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                .apply(bitmapTransform(new CircleCrop()))
                .into(mCircleImageView);
    }

    private void checkPermissionToLoadPicture() {
        if(getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION);
            } else {
                updatePicture();
            }
        }
    }
}
