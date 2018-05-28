package com.example.android.lab1.ui.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ViewPagerAdapter;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.BookPhotoDetailActivity;
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.example.android.lab1.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ProfileActivity extends AppCompatActivity {

    public static final String ADDRESS_KEY = "ADDRESS";
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 10;

    TextView mUsernameText;
    ImageView mEditButton;
    ImageView mBackArrow;
    ImageView mCircleImageView;
    //Toolbar mToolbar;
    FragmentManager mFt = null;

    public static User mUser;
    SharedPreferencesManager mSharedPreferencesManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);
        mUsernameText = findViewById(R.id.global_profile_name);
        mCircleImageView = findViewById(R.id.profile_image);
        mEditButton = findViewById(R.id.edit_profile_button);
        mBackArrow = findViewById(R.id.back_arrow_profile);

        Utilities.setupStatusBarColor(this);
        mUser = getIntent().getExtras().getParcelable("user");
        mFt = getSupportFragmentManager();

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null) {
                    Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(ADDRESS_KEY, mUser.getAddress());
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Devi essere loggato", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookPhoto bookPhoto = new BookPhoto(mUser.getImage(), "Profile image");
                Intent intent = new Intent(getApplicationContext(), BookPhotoDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(BookPhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                startActivity(intent);
            }
        });

        /*
        mToolbar = findViewById(R.id.toolbar_profile_activity);
        mToolbar.setTitle(R.string.title_profile);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.activity_profile_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                if (clickedId == R.id.action_edit) {
                    if(mFirebaseAuth.getCurrentUser() != null) {
                        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        if (mFirebaseAuth.getCurrentUser() != null) {
                            if (mUser != null) {
                                intent.putExtra(ADDRESS_KEY, mUser.getAddress());
                            }
                        }
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Devi essere loggato", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */
        /*mFirebaseAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);
        if (mFirebaseAuth.getCurrentUser() != null)
            mUserDocumentReference = mDb.collection("users").document(mFirebaseAuth.getUid());
        if (mUserDocumentReference != null) {
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

        }*/
        mSharedPreferencesManager = SharedPreferencesManager.getInstance(this);

        if(mUser != null) {
            checkPermissionToLoadPicture();
            updateUI();
        }

        ViewPager viewPager = findViewById(R.id.viewpager_profile);
        setupViewPager(viewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabs = findViewById(R.id.tabs_show_profile);
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());

        tabs.setupWithViewPager(viewPager);
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

    public static User getUser() {
        return mUser;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFt);
        adapter.addFragment(new ProfileInfoFragment(), getResources().getString(R.string.profile_info_fragment));
        adapter.addFragment(new ProfileReviewFragment(), getResources().getString(R.string.profile_reviews_fragment));
        viewPager.setAdapter(adapter);
    }

    private void updateUI() {
        if (mUsernameText.getText() != null) {
            if (mUser.getUsername() != null)
                mUsernameText.setText(mUser.getUsername());
        }
        if (mUser.getImage() != null) {
            mSharedPreferencesManager.putImage(mUser.getImage());
        }
    }

    private void updatePicture() {
        if (mUser.getImage() != null) {
            Glide.with(this).load(mUser.getImage())
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);

        } else {
            updateWithDefaultPicture();
        }
    }

    private void updateWithDefaultPicture() {
        Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                .apply(bitmapTransform(new CircleCrop()))
                .into(mCircleImageView);
    }

    @SuppressLint("NewApi")
    private void checkPermissionToLoadPicture() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION);
        } else {
            updatePicture();
        }
    }

}
