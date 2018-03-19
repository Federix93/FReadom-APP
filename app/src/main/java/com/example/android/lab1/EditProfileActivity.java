package com.example.android.lab1;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class EditProfileActivity extends AppCompatActivity {

    ImageView mCheckImageView;
    ImageView mCameraImageView;
    ImageView mUserImageView;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    public final int RESULT_LOAD_IMAGE = 1;
    public final int CAPTURE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mUsernameTextInputLayout = findViewById(R.id.username_text_edit);

        mEmailTextInputLayout = findViewById(R.id.email_text_edit);

        mPhoneTextInputLayout = findViewById(R.id.phone_text_edit);

        mUserImageView = findViewById(R.id.user_profile_edit_image);

        mCheckImageView = findViewById(R.id.icon_check_toolbar);

        mCameraImageView = findViewById(R.id.add_image_icon);

        if(savedInstanceState == null){
            if (UserManager.getUser().getUsername() != null && mUsernameTextInputLayout.getEditText() != null)
                mUsernameTextInputLayout.getEditText().setText(UserManager.getUser().getUsername());
            if (UserManager.getUser().getEmail() != null && mEmailTextInputLayout.getEditText() != null)
                mEmailTextInputLayout.getEditText().setText(UserManager.getUser().getEmail());
            if (UserManager.getUser().getPhone() != null && mPhoneTextInputLayout.getEditText() != null)
                mPhoneTextInputLayout.getEditText().setText(UserManager.getUser().getPhone());
            if(UserManager.getUser().getImage() != null && mUserImageView != null)
                Glide.with(getApplicationContext()).load(UserManager.getUser().getImage()).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
            else{
                Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserImageView);
            }
        }

        mCheckImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUsernameTextInputLayout.getEditText() != null)
                    UserManager.getUser().setUsername(mUsernameTextInputLayout.getEditText().getText().toString());
                if (mEmailTextInputLayout.getEditText() != null)
                UserManager.getUser().setEmail(mEmailTextInputLayout.getEditText().getText().toString());
                if (mPhoneTextInputLayout.getEditText() != null)
                UserManager.getUser().setPhone(mPhoneTextInputLayout.getEditText().getText().toString());
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
                chooser.putExtra(Intent.EXTRA_TITLE, "Select from:");

                Intent[] intentArray = { cameraIntent };
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooser, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mUsernameTextInputLayout.getEditText() != null)
            outState.putString("Username", mUsernameTextInputLayout.getEditText().getText().toString());
        if (mEmailTextInputLayout.getEditText() != null)
        outState.putString("Email", mEmailTextInputLayout.getEditText().getText().toString());
        if (mPhoneTextInputLayout.getEditText() != null)
        outState.putString("Phone", mPhoneTextInputLayout.getEditText().getText().toString());
        if(UserManager.getUser().getImage() != null)
            outState.putString("Bitmap", UserManager.getUser().getImage().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mUsernameTextInputLayout.getEditText() != null)
            mUsernameTextInputLayout.getEditText().setText(savedInstanceState.getString("Username"));
        if (mEmailTextInputLayout.getEditText() != null)
            mEmailTextInputLayout.getEditText().setText(savedInstanceState.getString("Email"));
        if (mPhoneTextInputLayout.getEditText() != null)
            mPhoneTextInputLayout.getEditText().setText(savedInstanceState.getString("Phone"));
        Glide.with(getApplicationContext()).load(savedInstanceState.getString("Bitmap")).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                Glide.with(getApplicationContext()).load(data.getData()).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                UserManager.getUser().setImage(data.getData());
        }else{
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mUserImageView.setImageBitmap(imageBitmap);
        }
    }
}
