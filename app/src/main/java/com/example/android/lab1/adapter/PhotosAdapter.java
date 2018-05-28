package com.example.android.lab1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder> {

    List<String> mPhotosPaths;
    private Context mContext;

    public PhotosAdapter(List<String> model) {
        mPhotosPaths = model;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null)
            mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_photo_square, parent, false);
        //v.getLayoutParams().width = parent.getWidth() / 3;
        return new PhotosViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        holder.setPhoto(mPhotosPaths.get(position));
    }

    @Override
    public int getItemCount() {
        return mPhotosPaths != null ? mPhotosPaths.size() : 0;
    }

    class PhotosViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton mRemovePhotoButton;
        private ImageView mPhotoImageView;

        public PhotosViewHolder(View v) {
            super(v);
            mPhotoImageView = v.findViewById(R.id.load_book_user_photo);
            mRemovePhotoButton = v.findViewById(R.id.load_book_remove_user_photo);
            mPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO open photo gallery activity
                }
            });
            mRemovePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition >= 0 && adapterPosition < getItemCount()) {
                        mPhotosPaths.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    }

                }
            });
        }

        public void setPhoto(String photoPath) {
            Glide.with(mContext)
                    .load(photoPath)
                    .into(mPhotoImageView);
        }
    }
}
