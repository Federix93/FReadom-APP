package com.example.android.lab1.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;

public class PhotosViewHolder extends RecyclerView.ViewHolder
{
    private final ImageButton mRemovePhotoButton;
    private ImageView mPhotoImageView;

    public PhotosViewHolder(View v, final Activity container)
    {
        super(v);
        mPhotoImageView = v.findViewById(R.id.load_book_user_photo);
        mRemovePhotoButton = v.findViewById(R.id.load_book_remove_user_photo);
        mPhotoImageView.setOnClickListener((View.OnClickListener) container);
    }

    public void setmRemovePhotoButton(final int position, final RemovePhotoClickListener listener)
    {
        mRemovePhotoButton.findViewById(R.id.load_book_remove_user_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.removePhoto(position);
            }
        });
    }


    public void setPhoto(String photo, Context applicationContext) {
        Glide.with(applicationContext)
                .load(photo)
                .into(mPhotoImageView);
    }
}
