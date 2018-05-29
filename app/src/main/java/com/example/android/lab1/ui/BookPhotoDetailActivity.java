package com.example.android.lab1.ui;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.android.lab1.R;
import com.example.android.lab1.model.BookPhoto;
import com.example.android.lab1.utils.Utilities;

public class BookPhotoDetailActivity extends AppCompatActivity{

    public static final String BOOK_PHOTO = "BOOK_PHOTO_DETAIL";
    private ImageView mImageView;
    Toolbar mToolbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full_detail);

        Utilities.setupStatusBarColor(this);

        mImageView = findViewById(R.id.image);
        mToolbar = findViewById(R.id.toolbar_library_book_detail);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        BookPhoto photo = getIntent().getParcelableExtra(BOOK_PHOTO);

        Glide.with(this)
                .asBitmap()
                .load(photo.getUrl())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        mImageView.setImageBitmap(resource);
                        return false;
                    }
                })
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_cloud_off_black_24dp))
                .into(mImageView);
    }
}
