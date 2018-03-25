package com.example.android.lab1;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class MainActivity extends AppCompatActivity {

    ImageView mToolbarIcon;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextInputLayout mAddressTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;
    ImageView mCircleImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsernameTextInputLayout = findViewById(R.id.username_text);

        mPhoneTextInputLayout = findViewById(R.id.phone_text);

        mEmailTextInputLayout = findViewById(R.id.email_text);

        mAddressTextInputLayout = findViewById(R.id.address_text_view);

        mCircleImageView = findViewById(R.id.profile_image);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        mToolbarIcon = findViewById(R.id.icon_toolbar);
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();

        if (mUsernameTextInputLayout.getEditText() != null) {
            if(user != null)
                mUsernameTextInputLayout.getEditText().setText(user.getUsername());
            mUsernameTextInputLayout.getEditText().setKeyListener(null);
            mUsernameTextInputLayout.getEditText().setEnabled(false);
        }
        if (mEmailTextInputLayout.getEditText() != null) {
            if(user != null)
                mEmailTextInputLayout.getEditText().setText(user.getEmail());
            mEmailTextInputLayout.getEditText().setKeyListener(null);
            mEmailTextInputLayout.getEditText().setEnabled(false);
        }
        if (mPhoneTextInputLayout.getEditText() != null) {
            if(user != null)
                mPhoneTextInputLayout.getEditText().setText(user.getPhone());
            mPhoneTextInputLayout.getEditText().setKeyListener(null);
            mPhoneTextInputLayout.getEditText().setEnabled(false);
        }

        if(mShortBioTextInputLayout.getEditText() != null){
            if(user != null)
                mShortBioTextInputLayout.getEditText().setText(user.getShortBio());
            mShortBioTextInputLayout.getEditText().setKeyListener(null);
            mShortBioTextInputLayout.getEditText().setEnabled(false);
        }

        if(user != null && user.getImage() != null) {
            Glide.with(this).load(user.getImage())
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        }else{
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        }

        if(mAddressTextInputLayout.getEditText() != null){
            if(user != null)
                mAddressTextInputLayout.getEditText().setText(user.getAddress());
            mAddressTextInputLayout.getEditText().setKeyListener(null);
            mAddressTextInputLayout.getEditText().setEnabled(false);
        }

        mToolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

    }
}
