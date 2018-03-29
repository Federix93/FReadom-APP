package com.example.android.lab1;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class EditProfileActivity extends AppCompatActivity {

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
    String mCurrentPhotoPath;
    private AlertDialog.Builder mAlertDialogBuilder = null;

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

        mAddressTextInputLayout = findViewById(R.id.position_text_input_layout);

        mShortBioTextInputLayout = findViewById(R.id.bio_text_edit);

        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();
        if(savedInstanceState == null)
        {
            if (user != null && user.getUsername() != null && mUsernameTextInputLayout.getEditText() != null)
                mUsernameTextInputLayout.getEditText().setText(user.getUsername());
            if (user != null && user.getEmail() != null && mEmailTextInputLayout.getEditText() != null)
                mEmailTextInputLayout.getEditText().setText(user.getEmail());
            if (user != null && user.getPhone() != null && mPhoneTextInputLayout.getEditText() != null)
                mPhoneTextInputLayout.getEditText().setText(user.getPhone());
            if(user != null && user.getShortBio() != null && mShortBioTextInputLayout.getEditText() != null)
                mShortBioTextInputLayout.getEditText().setText(user.getShortBio());
            if (user != null && user.getImage() != null && mUserImageView != null) {
                Glide.with(getApplicationContext()).load(user.getImage()).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                mCurrentPhotoPath = user.getImage();
            }else
                {
                Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                        .apply(bitmapTransform(new CircleCrop()))
                        .into(mUserImageView);
            }
            if( mAddressTextInputLayout != null)
            {
                if (user != null && user.getTempAddress() != null)
                {
                    mAddressTextInputLayout.setText(user.getTempAddress());
                }
                else{
                    mAddressTextInputLayout.setText(R.string.selection_position);
                }
            }
        }

        mSaveProfileUpdatesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(view.getContext());
                User user = sharedPreferencesManager.getUser();
                if(user == null)
                    user = User.getInstance();
                if (mUsernameTextInputLayout.getEditText() != null)
                    user.setUsername(mUsernameTextInputLayout.getEditText().getText().toString());
                if (mEmailTextInputLayout.getEditText() != null)
                    user.setEmail(mEmailTextInputLayout.getEditText().getText().toString());
                if (mPhoneTextInputLayout.getEditText() != null)
                    user.setPhone(mPhoneTextInputLayout.getEditText().getText().toString());
                if (mAddressTextInputLayout != null && user.getTempAddress() != null)
                    user.setAddress(mAddressTextInputLayout.getText().toString());
                if(mShortBioTextInputLayout.getEditText() != null)
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
                final CharSequence[] items = { getString(R.string.camera_option_dialog) ,getString(R.string.gallery_option_dialog)};
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
                        switch(i) {
                            case CAPTURE_IMAGE:
                                File photoFile = null;
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (cameraIntent.resolveActivity(getPackageManager()) != null){
                                    try {
                                        photoFile = saveThumbnail();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if(photoFile != null) {
                                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                                "com.example.android.fileprovider",
                                                photoFile);
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
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PositionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();
        user.setTempAddress(user.getAddress());
        sharedPreferencesManager.putUser(user);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User user = sharedPreferencesManager.getUser();
        if( mAddressTextInputLayout != null)
        {
            if (user != null)
            {
                mAddressTextInputLayout.setText(user.getTempAddress());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mUsernameTextInputLayout.getEditText() != null)
            outState.putString("Username", mUsernameTextInputLayout.getEditText().getText().toString());
        if (mEmailTextInputLayout.getEditText() != null)
            outState.putString("Email", mEmailTextInputLayout.getEditText().getText().toString());
        if (mPhoneTextInputLayout.getEditText() != null)
            outState.putString("Phone", mPhoneTextInputLayout.getEditText().getText().toString());
        if (mAddressTextInputLayout != null)
            outState.putString("Address", mAddressTextInputLayout.getText().toString());
        if(mShortBioTextInputLayout.getEditText() != null)
            outState.putString("ShortBio", mShortBioTextInputLayout.getEditText().getText().toString());
        outState.putString("UriImage", mCurrentPhotoPath);
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
        if (mAddressTextInputLayout != null)
            mAddressTextInputLayout.setText(savedInstanceState.getString("Address"));
        if(mShortBioTextInputLayout.getEditText() != null)
            mShortBioTextInputLayout.getEditText().setText(savedInstanceState.getString("ShortBio"));
        mCurrentPhotoPath = savedInstanceState.getString("UriImage");
        if(mCurrentPhotoPath != null)
            Glide.with(getApplicationContext()).load(mCurrentPhotoPath)
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImageView);
        else
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mUserImageView);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imageURI = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            if(data.getData() != null) {
                imageURI = data.getData().toString();
                mCurrentPhotoPath = imageURI;
                Glide.with(getApplicationContext()).load(imageURI).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
            }
        }else if(requestCode == CAPTURE_IMAGE && resultCode == RESULT_OK){
            if(mCurrentPhotoPath != null) {
                imageURI = mCurrentPhotoPath;
                Glide.with(getApplicationContext()).load(imageURI).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
            }
        }
    }

    public File saveThumbnail() throws IOException{

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
