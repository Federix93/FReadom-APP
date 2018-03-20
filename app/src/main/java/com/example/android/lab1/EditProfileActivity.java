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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class EditProfileActivity extends AppCompatActivity {

    ImageView mCheckImageView;
    ImageView mCameraImageView;
    ImageView mUserImageView;
    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    private final int RESULT_LOAD_IMAGE = 1;
    private final int CAPTURE_IMAGE = 0;
    String mCurrentPhotoPath;

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


        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        User mUser = sharedPreferencesManager.getUser();
        UserManager.setUser(mUser);
        if(savedInstanceState == null){

            if(mUser != null) {
                if (mUser.getUsername() != null && mUsernameTextInputLayout.getEditText() != null)
                    mUsernameTextInputLayout.getEditText().setText(mUser.getUsername());
                if (mUser.getEmail() != null && mEmailTextInputLayout.getEditText() != null)
                    mEmailTextInputLayout.getEditText().setText(mUser.getEmail());
                if (mUser.getPhone() != null && mPhoneTextInputLayout.getEditText() != null)
                    mPhoneTextInputLayout.getEditText().setText(mUser.getPhone());
                Log.d("LULLO", "IMAGE " + mUser.getImage());
                if (mUser.getImage() != null && mUserImageView != null)
                    Glide.with(getApplicationContext()).load(mUser.getImage()).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                else {
                    Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                            .apply(bitmapTransform(new CircleCrop()))
                            .into(mUserImageView);
                }
            }
        }

        mCheckImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(view.getContext());
                User user = sharedPreferencesManager.getUser();
                UserManager.getUser().setImage(user.getImage());
                if (mUsernameTextInputLayout.getEditText() != null)
                    UserManager.getUser().setUsername(mUsernameTextInputLayout.getEditText().getText().toString());
                if (mEmailTextInputLayout.getEditText() != null)
                    UserManager.getUser().setEmail(mEmailTextInputLayout.getEditText().getText().toString());
                if (mPhoneTextInputLayout.getEditText() != null)
                    UserManager.getUser().setPhone(mPhoneTextInputLayout.getEditText().getText().toString());
                sharedPreferencesManager.putUser();
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] items = { getString(R.string.camera_option_dialog) ,getString(R.string.gallery_option_dialog)};
                final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
                dialog.setNegativeButton(R.string.negative_button_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dialog.setTitle(R.string.title_dialog).setItems(items, new DialogInterface.OnClickListener() {
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
            outState.putString("Bitmap", UserManager.getUser().getImage());
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
        String imageURI = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            if(data.getData() != null) {
                imageURI = data.getData().toString();
                Glide.with(getApplicationContext()).load(imageURI).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                UserManager.getUser().setImage(imageURI);
            }
        }else if(requestCode == CAPTURE_IMAGE && resultCode == RESULT_OK){
            if(mCurrentPhotoPath != null) {
                imageURI = mCurrentPhotoPath;
                Glide.with(getApplicationContext()).load(imageURI).apply(bitmapTransform(new CircleCrop())).into(mUserImageView);
                UserManager.getUser().setImage(imageURI);
            }
        }
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        sharedPreferencesManager.putUser();
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
