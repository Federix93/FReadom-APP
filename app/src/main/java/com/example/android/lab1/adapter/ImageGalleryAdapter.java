package com.example.android.lab1.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.BookPhoto;

import com.example.android.lab1.ui.BookPhotoDetailActivity;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder> {

    private BookPhoto[] mBookPhoto;
    private Context mContext;

    public ImageGalleryAdapter(BookPhoto[] mBookPhoto, Context mContext) {
        this.mBookPhoto = mBookPhoto;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View photoView = inflater.inflate(R.layout.book_detail_gallery_item, parent, false);
        int width = (parent.getMeasuredWidth() / 3) - 16;
        int height = parent.getMeasuredHeight();
        photoView.setLayoutParams(new RecyclerView.LayoutParams(width, height));

        return new MyViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BookPhoto bookPhoto = mBookPhoto[position];
        ImageView imageView = holder.mPhotoImageView;

        Glide.with(mContext)
                .load(bookPhoto.getUrl())
                .apply(new RequestOptions().override(400, 400).fitCenter()
                        .placeholder(R.drawable.ic_no_book_photo))
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return mBookPhoto.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView mPhotoImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mPhotoImageView = itemView.findViewById(R.id.iv_photo);
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                BookPhoto bookPhoto = mBookPhoto[position];
                Intent intent = new Intent(mContext, BookPhotoDetailActivity.class);
                intent.putExtra(BookPhotoDetailActivity.BOOK_PHOTO, bookPhoto);
                mContext.startActivity(intent);
            }
        }
    }
}
