package com.example.android.lab1.adapter;

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

    public PhotosViewHolder(View v, View.OnClickListener photoTouch, final RemovePhotoClickListener removePhotoClickListener)
    {
        super(v);
        mPhotoImageView = v.findViewById(R.id.load_book_user_photo);
        mRemovePhotoButton = v.findViewById(R.id.load_book_remove_user_photo);
        mPhotoImageView.setOnClickListener(photoTouch);
        mRemovePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePhotoClickListener.removePhoto(getAdapterPosition());
            }
        });
    }


    public void setPhoto(String photo, Context applicationContext) {
        Glide.with(applicationContext)
                .load(photo)
                .into(mPhotoImageView);
    }
}
